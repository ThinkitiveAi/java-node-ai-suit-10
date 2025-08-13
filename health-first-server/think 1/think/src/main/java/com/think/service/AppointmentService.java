package com.think.service;

import com.think.dto.*;
import com.think.entity.AppointmentSlot;
import com.think.entity.Patient;
import com.think.entity.Provider;
import com.think.entity.ProviderAvailability;
import com.think.repository.AppointmentSlotRepository;
import com.think.repository.PatientRepository;
import com.think.repository.ProviderAvailabilityRepository;
import com.think.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {
    
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final ProviderAvailabilityRepository availabilityRepository;
    
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        log.info("Booking appointment for patient: {}, provider: {}, date: {}, time: {}", 
                request.getPatientId(), request.getProviderId(), request.getAppointmentDate(), request.getAppointmentTime());
        
        // Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + request.getPatientId()));
        
        // Validate provider exists
        Provider provider = providerRepository.findById(UUID.fromString(request.getProviderId()))
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with ID: " + request.getProviderId()));
        
        // Create appointment datetime
        LocalDateTime appointmentDateTime = LocalDateTime.of(request.getAppointmentDate(), request.getAppointmentTime());
        
        // Validate appointment is in the future
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled in the past");
        }
        
        // Check for booking conflicts
        if (appointmentSlotRepository.countBookedSlotsByProviderAndTime(UUID.fromString(request.getProviderId()), appointmentDateTime) > 0) {
            throw new IllegalArgumentException("This time slot is already booked");
        }
        
        // Find available slot
        Optional<AppointmentSlot> availableSlot = appointmentSlotRepository.findAvailableSlotByProviderAndTime(
                UUID.fromString(request.getProviderId()), appointmentDateTime);
        
        if (availableSlot.isEmpty()) {
            throw new IllegalArgumentException("No available slot found for the requested time");
        }
        
        AppointmentSlot slot = availableSlot.get();
        
        // Check if slot is within provider's availability
        if (!isSlotWithinProviderAvailability(slot, appointmentDateTime)) {
            throw new IllegalArgumentException("Requested time is not within provider's availability");
        }
        
        // Book the appointment
        slot.setPatient(patient);
        slot.setStatus(AppointmentSlot.SlotStatus.BOOKED);
        slot.setAppointmentType(request.getAppointmentType());
        
        // Calculate estimated cost
        BigDecimal estimatedCost = calculateEstimatedCost(slot, request.getAppointmentType());
        
        AppointmentSlot savedSlot = appointmentSlotRepository.save(slot);
        
        log.info("Appointment booked successfully with booking reference: {}", savedSlot.getBookingReference());
        
        return createAppointmentResponse(savedSlot, patient, provider, estimatedCost, request);
    }
    
    public AppointmentListResponse getAppointments(AppointmentListRequest request) {
        log.info("Retrieving appointments with filters: startDate={}, endDate={}, appointmentType={}, providerId={}, patientId={}, status={}", 
                request.getStartDate(), request.getEndDate(), request.getAppointmentType(), 
                request.getProviderId(), request.getPatientId(), request.getStatus());
        
        try {
            // Create pageable
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // Convert dates to LocalDateTime for query
            LocalDateTime startDateTime = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
            LocalDateTime endDateTime = request.getEndDate() != null ? request.getEndDate().atTime(LocalTime.MAX) : null;
            
            Page<AppointmentSlot> appointmentPage = appointmentSlotRepository.findAppointmentsWithFilters(
                    startDateTime, endDateTime, request.getAppointmentType(), 
                    request.getProviderId(), request.getPatientId(), request.getStatus(), pageable);
            
            // Convert to response
            Page<AppointmentResponse> responsePage = appointmentPage.map(this::convertToAppointmentResponse);
            
            return new AppointmentListResponse(
                    responsePage.getContent(),
                    responsePage.getNumber(),
                    responsePage.getTotalPages(),
                    responsePage.getTotalElements(),
                    responsePage.getSize(),
                    responsePage.hasNext(),
                    responsePage.hasPrevious()
            );
        } catch (Exception e) {
            log.error("Error retrieving appointments", e);
            // Return empty response instead of throwing exception
            return new AppointmentListResponse(
                    new ArrayList<>(),
                    0,
                    0,
                    0L,
                    request.getSize(),
                    false,
                    false
            );
        }
    }
    
    public AppointmentResponse getAppointmentByBookingReference(String bookingReference) {
        log.info("Retrieving appointment with booking reference: {}", bookingReference);
        
        AppointmentSlot slot = appointmentSlotRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with booking reference: " + bookingReference));
        
        return convertToAppointmentResponse(slot);
    }
    
    public AppointmentResponse cancelAppointment(String bookingReference) {
        log.info("Cancelling appointment with booking reference: {}", bookingReference);
        
        AppointmentSlot slot = appointmentSlotRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with booking reference: " + bookingReference));
        
        if (slot.getStatus() != AppointmentSlot.SlotStatus.BOOKED) {
            throw new IllegalArgumentException("Appointment is not in BOOKED status and cannot be cancelled");
        }
        
        slot.setStatus(AppointmentSlot.SlotStatus.CANCELLED);
        slot.setPatient(null);
        slot.setAppointmentType(null);
        
        AppointmentSlot savedSlot = appointmentSlotRepository.save(slot);
        
        log.info("Appointment cancelled successfully: {}", bookingReference);
        
        return convertToAppointmentResponse(savedSlot);
    }
    
    private boolean isSlotWithinProviderAvailability(AppointmentSlot slot, LocalDateTime appointmentDateTime) {
        // Check if the appointment time falls within the slot's time range
        return !appointmentDateTime.isBefore(slot.getSlotStartTime()) && 
               !appointmentDateTime.isAfter(slot.getSlotEndTime());
    }
    
    private BigDecimal calculateEstimatedCost(AppointmentSlot slot, String appointmentType) {
        // Get the base fee from provider availability
        ProviderAvailability availability = slot.getAvailability();
        BigDecimal baseFee = availability.getPricing() != null ? availability.getPricing().getBaseFee() : BigDecimal.valueOf(100.00);
        
        // Apply multipliers based on appointment type
        switch (appointmentType) {
            case "EMERGENCY":
                return baseFee.multiply(BigDecimal.valueOf(1.5));
            case "TELEMEDICINE":
                return baseFee.multiply(BigDecimal.valueOf(0.8));
            case "FOLLOW_UP":
                return baseFee.multiply(BigDecimal.valueOf(0.9));
            default: // CONSULTATION
                return baseFee;
        }
    }
    
    private AppointmentResponse convertToAppointmentResponse(AppointmentSlot slot) {
        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(slot.getId());
        response.setBookingReference(slot.getBookingReference());
        response.setAppointmentDateTime(slot.getSlotStartTime());
        response.setAppointmentDate(slot.getSlotStartTime().toLocalDate());
        response.setAppointmentTime(slot.getSlotStartTime().toLocalTime());
        response.setAppointmentType(slot.getAppointmentType());
        response.setStatus(slot.getStatus().name());
        response.setCreatedAt(slot.getCreatedAt());
        response.setUpdatedAt(slot.getUpdatedAt());
        
        if (slot.getPatient() != null) {
            response.setPatientId(slot.getPatient().getId());
            response.setPatientName(slot.getPatient().getFirstName() + " " + slot.getPatient().getLastName());
            response.setPatientEmail(slot.getPatient().getEmail());
            response.setPatientPhone(slot.getPatient().getPhoneNumber());
            response.setPatientGender(slot.getPatient().getGender().name());
            response.setPatientDateOfBirth(slot.getPatient().getDateOfBirth());
            response.setPatientAddress(formatPatientAddress(slot.getPatient()));
        }
        
        if (slot.getProvider() != null) {
            response.setProviderId(slot.getProvider().getId().toString());
            response.setProviderName(slot.getProvider().getFirstName() + " " + slot.getProvider().getLastName());
            response.setProviderSpecialization(slot.getProvider().getSpecialization());
            response.setProviderEmail(slot.getProvider().getEmail());
            response.setProviderPhone(slot.getProvider().getPhoneNumber());
            response.setClinicAddress(formatClinicAddress(slot.getProvider()));
        }
        
        // Calculate estimated cost
        if (slot.getAppointmentType() != null) {
            response.setEstimatedCost(calculateEstimatedCost(slot, slot.getAppointmentType()));
            response.setCurrency("USD"); // Default currency
        }
        
        return response;
    }
    
    private AppointmentResponse createAppointmentResponse(AppointmentSlot slot, Patient patient, Provider provider, 
                                                        BigDecimal estimatedCost, BookAppointmentRequest request) {
        AppointmentResponse response = convertToAppointmentResponse(slot);
        response.setAppointmentMode(request.getAppointmentMode());
        response.setReasonForVisit(request.getReasonForVisit());
        response.setAdditionalNotes(request.getAdditionalNotes());
        response.setInsuranceProvider(request.getInsuranceProvider());
        response.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        response.setEstimatedCost(estimatedCost);
        response.setCurrency("USD");
        return response;
    }
    
    private String formatPatientAddress(Patient patient) {
        if (patient.getAddress() == null) return "";
        StringBuilder address = new StringBuilder();
        if (patient.getAddress().getStreet() != null) address.append(patient.getAddress().getStreet()).append(", ");
        if (patient.getAddress().getCity() != null) address.append(patient.getAddress().getCity()).append(", ");
        if (patient.getAddress().getState() != null) address.append(patient.getAddress().getState()).append(" ");
        if (patient.getAddress().getZip() != null) address.append(patient.getAddress().getZip());
        return address.toString().trim();
    }
    
    private String formatClinicAddress(Provider provider) {
        if (provider.getClinicAddress() == null) return "";
        StringBuilder address = new StringBuilder();
        if (provider.getClinicAddress().getStreet() != null) address.append(provider.getClinicAddress().getStreet()).append(", ");
        if (provider.getClinicAddress().getCity() != null) address.append(provider.getClinicAddress().getCity()).append(", ");
        if (provider.getClinicAddress().getState() != null) address.append(provider.getClinicAddress().getState()).append(" ");
        if (provider.getClinicAddress().getZip() != null) address.append(provider.getClinicAddress().getZip());
        return address.toString().trim();
    }
}
