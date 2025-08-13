package com.think.service;

import com.think.dto.AppointmentListRequest;
import com.think.dto.AppointmentListResponse;
import com.think.dto.AppointmentResponse;
import com.think.dto.BookAppointmentRequest;
import com.think.entity.*;
import com.think.repository.AppointmentSlotRepository;
import com.think.repository.PatientRepository;
import com.think.repository.ProviderAvailabilityRepository;
import com.think.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentSlotRepository appointmentSlotRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderAvailabilityRepository availabilityRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Provider testProvider;
    private ProviderAvailability testAvailability;
    private AppointmentSlot testSlot;
    private BookAppointmentRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setId("patient-123");
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("john.doe@example.com");
        testPatient.setPhoneNumber("+1234567890");
        testPatient.setGender(Patient.Gender.MALE);
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        PatientAddress address = new PatientAddress();
        address.setStreet("123 Main St");
        address.setCity("New York");
        address.setState("NY");
        address.setZip("10001");
        testPatient.setAddress(address);

        // Setup test provider
        testProvider = new Provider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setFirstName("Dr. Jane");
        testProvider.setLastName("Smith");
        testProvider.setEmail("jane.smith@example.com");
        testProvider.setPhoneNumber("+1987654321");
        testProvider.setSpecialization("Cardiology");
        
        ClinicAddress clinicAddress = new ClinicAddress();
        clinicAddress.setStreet("456 Medical Center Dr");
        clinicAddress.setCity("New York");
        clinicAddress.setState("NY");
        clinicAddress.setZip("10002");
        testProvider.setClinicAddress(clinicAddress);

        // Setup test availability
        testAvailability = new ProviderAvailability();
        testAvailability.setId("availability-123");
        testAvailability.setProvider(testProvider);
        testAvailability.setDate(LocalDate.of(2024, 2, 15));
        testAvailability.setStartTime(LocalTime.of(9, 0));
        testAvailability.setEndTime(LocalTime.of(17, 0));
        testAvailability.setTimezone("America/New_York");
        
        AvailabilityPricing pricing = new AvailabilityPricing();
        pricing.setBaseFee(BigDecimal.valueOf(150.00));
        pricing.setInsuranceAccepted(true);
        pricing.setCurrency("USD");
        testAvailability.setPricing(pricing);

        // Setup test slot
        testSlot = new AppointmentSlot();
        testSlot.setId("slot-123");
        testSlot.setAvailability(testAvailability);
        testSlot.setProvider(testProvider);
        testSlot.setSlotStartTime(LocalDateTime.of(2024, 2, 15, 10, 0));
        testSlot.setSlotEndTime(LocalDateTime.of(2024, 2, 15, 10, 30));
        testSlot.setStatus(AppointmentSlot.SlotStatus.AVAILABLE);
        testSlot.setBookingReference("APT-12345678");

        // Setup test request
        testRequest = new BookAppointmentRequest();
        testRequest.setPatientId(UUID.fromString("patient-123"));
        testRequest.setProviderId(testProvider.getId());
        testRequest.setAppointmentDate(LocalDate.of(2024, 2, 15));
        testRequest.setAppointmentTime(LocalTime.of(10, 0));
        testRequest.setAppointmentType("CONSULTATION");
        testRequest.setAppointmentMode("IN_PERSON");
        testRequest.setReasonForVisit("Regular checkup and consultation");
        testRequest.setAdditionalNotes("Patient prefers morning appointments");
        testRequest.setInsuranceProvider("Blue Cross");
        testRequest.setInsurancePolicyNumber("BC123456");
    }

    @Test
    void bookAppointment_Success() {
        // Arrange
        when(patientRepository.findById("patient-123")).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(appointmentSlotRepository.countBookedSlotsByProviderAndTime(any(), any())).thenReturn(0L);
        when(appointmentSlotRepository.findAvailableSlotByProviderAndTime(any(), any())).thenReturn(Optional.of(testSlot));
        when(appointmentSlotRepository.save(any())).thenReturn(testSlot);

        // Act
        AppointmentResponse response = appointmentService.bookAppointment(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("slot-123", response.getAppointmentId());
        assertEquals("APT-12345678", response.getBookingReference());
        assertEquals("John Doe", response.getPatientName());
        assertEquals("Dr. Jane Smith", response.getProviderName());
        assertEquals("CONSULTATION", response.getAppointmentType());
        assertEquals("IN_PERSON", response.getAppointmentMode());
        assertEquals("Regular checkup and consultation", response.getReasonForVisit());
        assertEquals(BigDecimal.valueOf(150.00), response.getEstimatedCost());
        assertEquals("USD", response.getCurrency());

        verify(appointmentSlotRepository).save(any(AppointmentSlot.class));
    }

    @Test
    void bookAppointment_PatientNotFound() {
        // Arrange
        when(patientRepository.findById("patient-123")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.bookAppointment(testRequest));
        assertEquals("Patient not found with ID: " + testRequest.getPatientId(), exception.getMessage());
    }

    @Test
    void bookAppointment_ProviderNotFound() {
        // Arrange
        when(patientRepository.findById("patient-123")).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.bookAppointment(testRequest));
        assertEquals("Provider not found with ID: " + testRequest.getProviderId(), exception.getMessage());
    }

    @Test
    void bookAppointment_PastAppointment() {
        // Arrange
        testRequest.setAppointmentDate(LocalDate.now().minusDays(1));
        when(patientRepository.findById("patient-123")).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.bookAppointment(testRequest));
        assertEquals("Appointment cannot be scheduled in the past", exception.getMessage());
    }

    @Test
    void bookAppointment_BookingConflict() {
        // Arrange
        when(patientRepository.findById("patient-123")).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(appointmentSlotRepository.countBookedSlotsByProviderAndTime(any(), any())).thenReturn(1L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.bookAppointment(testRequest));
        assertEquals("This time slot is already booked", exception.getMessage());
    }

    @Test
    void bookAppointment_NoAvailableSlot() {
        // Arrange
        when(patientRepository.findById("patient-123")).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(appointmentSlotRepository.countBookedSlotsByProviderAndTime(any(), any())).thenReturn(0L);
        when(appointmentSlotRepository.findAvailableSlotByProviderAndTime(any(), any())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.bookAppointment(testRequest));
        assertEquals("No available slot found for the requested time", exception.getMessage());
    }

    @Test
    void getAppointments_Success() {
        // Arrange
        AppointmentListRequest request = new AppointmentListRequest();
        request.setPage(0);
        request.setSize(20);
        request.setSortBy("appointmentDateTime");
        request.setSortDirection("DESC");

        Page<AppointmentSlot> page = new PageImpl<>(List.of(testSlot), PageRequest.of(0, 20), 1);
        when(appointmentSlotRepository.findAppointmentsWithFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // Act
        AppointmentListResponse response = appointmentService.getAppointments(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getAppointments().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
        assertEquals(20, response.getPageSize());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    void getAppointmentByBookingReference_Success() {
        // Arrange
        when(appointmentSlotRepository.findByBookingReference("APT-12345678")).thenReturn(Optional.of(testSlot));

        // Act
        AppointmentResponse response = appointmentService.getAppointmentByBookingReference("APT-12345678");

        // Assert
        assertNotNull(response);
        assertEquals("slot-123", response.getAppointmentId());
        assertEquals("APT-12345678", response.getBookingReference());
    }

    @Test
    void getAppointmentByBookingReference_NotFound() {
        // Arrange
        when(appointmentSlotRepository.findByBookingReference("APT-12345678")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.getAppointmentByBookingReference("APT-12345678"));
        assertEquals("Appointment not found with booking reference: APT-12345678", exception.getMessage());
    }

    @Test
    void cancelAppointment_Success() {
        // Arrange
        testSlot.setStatus(AppointmentSlot.SlotStatus.BOOKED);
        testSlot.setPatient(testPatient);
        testSlot.setAppointmentType("CONSULTATION");
        
        when(appointmentSlotRepository.findByBookingReference("APT-12345678")).thenReturn(Optional.of(testSlot));
        when(appointmentSlotRepository.save(any())).thenReturn(testSlot);

        // Act
        AppointmentResponse response = appointmentService.cancelAppointment("APT-12345678");

        // Assert
        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(appointmentSlotRepository).save(any(AppointmentSlot.class));
    }

    @Test
    void cancelAppointment_NotFound() {
        // Arrange
        when(appointmentSlotRepository.findByBookingReference("APT-12345678")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.cancelAppointment("APT-12345678"));
        assertEquals("Appointment not found with booking reference: APT-12345678", exception.getMessage());
    }

    @Test
    void cancelAppointment_NotBooked() {
        // Arrange
        testSlot.setStatus(AppointmentSlot.SlotStatus.CANCELLED);
        when(appointmentSlotRepository.findByBookingReference("APT-12345678")).thenReturn(Optional.of(testSlot));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> appointmentService.cancelAppointment("APT-12345678"));
        assertEquals("Appointment is not in BOOKED status and cannot be cancelled", exception.getMessage());
    }
}
