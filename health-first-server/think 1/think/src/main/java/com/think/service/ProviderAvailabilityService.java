package com.think.service;

import com.think.dto.*;
import com.think.entity.*;
import com.think.repository.AppointmentSlotRepository;
import com.think.repository.ProviderAvailabilityRepository;
import com.think.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderAvailabilityService {
    
    private final ProviderAvailabilityRepository availabilityRepository;
    private final AppointmentSlotRepository slotRepository;
    private final ProviderRepository providerRepository;
    
    @Transactional
    public AvailabilityResponse createAvailability(String providerId, CreateAvailabilityRequest request) {
        log.info("Creating availability for provider: {}", providerId);
        
        UUID providerUuid;
        try {
            providerUuid = UUID.fromString(providerId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid provider ID format: " + providerId);
        }
        
        Provider provider = providerRepository.findById(providerUuid)
            .orElseThrow(() -> new IllegalArgumentException("Provider not found with ID: " + providerId));
        
        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Check for overlapping slots
        List<ProviderAvailability> overlappingSlots = availabilityRepository.findOverlappingSlots(
            providerUuid, request.getDate(), request.getStartTime(), request.getEndTime());
        
        if (!overlappingSlots.isEmpty()) {
            throw new IllegalArgumentException("Time slot overlaps with existing availability");
        }
        
        // Create availability entity
        ProviderAvailability availability = buildAvailabilityEntity(provider, request);
        ProviderAvailability savedAvailability = availabilityRepository.save(availability);
        
        // Generate appointment slots
        List<AppointmentSlot> slots = generateAppointmentSlots(savedAvailability);
        slotRepository.saveAll(slots);
        
        // Calculate total appointments
        int totalAppointments = slots.size() * availability.getMaxAppointmentsPerSlot();
        
        return AvailabilityResponse.builder()
            .availabilityId(savedAvailability.getId())
            .slotsCreated(slots.size())
            .dateRange(new AvailabilityResponse.DateRange(request.getDate(), 
                request.getRecurrenceEndDate() != null ? request.getRecurrenceEndDate() : request.getDate()))
            .totalAppointmentsAvailable(totalAppointments)
            .build();
    }
    
    @Transactional(readOnly = true)
    public AvailabilityResponse.ProviderAvailabilityResponse getProviderAvailability(
            String providerId, LocalDate startDate, LocalDate endDate, 
            ProviderAvailability.AvailabilityStatus status, 
            ProviderAvailability.AppointmentType appointmentType) {
        
        log.info("Getting availability for provider: {} from {} to {}", providerId, startDate, endDate);
        
        List<ProviderAvailability> availabilities = availabilityRepository.findAvailabilityWithFilters(
            UUID.fromString(providerId), startDate, endDate, status, appointmentType);
        
        // Group by date and convert to response format
        Map<LocalDate, List<ProviderAvailability>> groupedByDate = availabilities.stream()
            .collect(Collectors.groupingBy(ProviderAvailability::getDate));
        
        List<AvailabilityResponse.DailyAvailability> dailyAvailabilities = groupedByDate.entrySet().stream()
            .map(entry -> buildDailyAvailability(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(AvailabilityResponse.DailyAvailability::getDate))
            .collect(Collectors.toList());
        
        // Calculate summary
        AvailabilityResponse.AvailabilitySummary summary = calculateAvailabilitySummary(availabilities);
        
        return AvailabilityResponse.ProviderAvailabilityResponse.builder()
            .providerId(providerId)
            .availabilitySummary(summary)
            .availability(dailyAvailabilities)
            .build();
    }
    
    @Transactional(readOnly = true)
    public AvailabilitySearchResponse searchAvailableSlots(AvailabilitySearchRequest request) {
        log.info("Searching available slots with criteria: {}", request);
        
        // Set default date range if not provided
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        if (request.getDate() != null) {
            startDate = request.getDate();
            endDate = request.getDate();
        } else if (startDate == null || endDate == null) {
            startDate = LocalDate.now();
            endDate = startDate.plusDays(30);
        }
        
        Pageable pageable = PageRequest.of(0, 50); // Limit results
        
        Page<ProviderAvailability> availabilities = availabilityRepository.searchAvailableSlots(
            request.getSpecialization(),
            startDate,
            endDate,
            request.getAppointmentType() != null ? 
                ProviderAvailability.AppointmentType.valueOf(request.getAppointmentType().toUpperCase()) : null,
            request.getInsuranceAccepted(),
            request.getMaxPrice() != null ? request.getMaxPrice().doubleValue() : null,
            pageable
        );
        
        // Convert to search response
        List<AvailabilitySearchResponse.SearchResult> results = availabilities.getContent().stream()
            .collect(Collectors.groupingBy(ProviderAvailability::getProvider))
            .entrySet().stream()
            .map(entry -> buildSearchResult(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        
        return AvailabilitySearchResponse.builder()
            .searchCriteria(buildSearchCriteria(request))
            .totalResults((int) availabilities.getTotalElements())
            .results(results)
            .build();
    }
    
    @Transactional
    public void updateAvailabilitySlot(String slotId, String providerId, Map<String, Object> updates) {
        log.info("Updating availability slot: {} for provider: {}", slotId, providerId);
        
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        
        if (!slot.getProvider().getId().toString().equals(providerId)) {
            throw new IllegalArgumentException("Slot does not belong to provider");
        }
        
        // Apply updates
        if (updates.containsKey("start_time")) {
            slot.setSlotStartTime(LocalDateTime.parse((String) updates.get("start_time")));
        }
        if (updates.containsKey("end_time")) {
            slot.setSlotEndTime(LocalDateTime.parse((String) updates.get("end_time")));
        }
        if (updates.containsKey("status")) {
            slot.setStatus(AppointmentSlot.SlotStatus.valueOf((String) updates.get("status")));
        }
        if (updates.containsKey("notes")) {
            // Update the parent availability notes
            ProviderAvailability availability = slot.getAvailability();
            availability.setNotes((String) updates.get("notes"));
            availabilityRepository.save(availability);
        }
        
        slotRepository.save(slot);
    }
    
    @Transactional
    public void deleteAvailabilitySlot(String slotId, String providerId, boolean deleteRecurring, String reason) {
        log.info("Deleting availability slot: {} for provider: {}", slotId, providerId);
        
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        
        if (!slot.getProvider().getId().toString().equals(providerId)) {
            throw new IllegalArgumentException("Slot does not belong to provider");
        }
        
        if (slot.getStatus() == AppointmentSlot.SlotStatus.BOOKED) {
            throw new IllegalArgumentException("Cannot delete booked slot");
        }
        
        if (deleteRecurring && slot.getAvailability().getIsRecurring()) {
            // Delete all recurring slots
            List<AppointmentSlot> recurringSlots = slotRepository.findAvailableSlotsByAvailabilityId(
                slot.getAvailability().getId());
            slotRepository.deleteAll(recurringSlots);
            availabilityRepository.delete(slot.getAvailability());
        } else {
            slotRepository.delete(slot);
        }
    }
    
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        Duration duration = Duration.between(startTime, endTime);
        if (duration.toMinutes() < 15) {
            throw new IllegalArgumentException("Time slot must be at least 15 minutes");
        }
    }
    
    private ProviderAvailability buildAvailabilityEntity(Provider provider, CreateAvailabilityRequest request) {
        return ProviderAvailability.builder()
            .provider(provider)
            .date(request.getDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .timezone(request.getTimezone())
            .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
            .recurrencePattern(request.getRecurrencePattern())
            .recurrenceEndDate(request.getRecurrenceEndDate())
            .slotDuration(request.getSlotDuration() != null ? request.getSlotDuration() : 30)
            .breakDuration(request.getBreakDuration() != null ? request.getBreakDuration() : 0)
            .maxAppointmentsPerSlot(request.getMaxAppointmentsPerSlot() != null ? request.getMaxAppointmentsPerSlot() : 1)
            .currentAppointments(0)
            .status(ProviderAvailability.AvailabilityStatus.AVAILABLE)
            .appointmentType(request.getAppointmentType() != null ? request.getAppointmentType() : ProviderAvailability.AppointmentType.CONSULTATION)
            .location(buildLocation(request.getLocation()))
            .pricing(buildPricing(request.getPricing()))
            .notes(request.getNotes())
            .specialRequirements(request.getSpecialRequirements())
            .build();
    }
    
    private AvailabilityLocation buildLocation(CreateAvailabilityRequest.LocationRequest locationRequest) {
        return AvailabilityLocation.builder()
            .type(locationRequest.getType())
            .address(locationRequest.getAddress())
            .roomNumber(locationRequest.getRoomNumber())
            .build();
    }
    
    private AvailabilityPricing buildPricing(CreateAvailabilityRequest.PricingRequest pricingRequest) {
        if (pricingRequest == null) {
            return AvailabilityPricing.builder()
                .baseFee(BigDecimal.valueOf(100.00))
                .insuranceAccepted(false)
                .currency("USD")
                .build();
        }
        return AvailabilityPricing.builder()
            .baseFee(pricingRequest.getBaseFee() != null ? pricingRequest.getBaseFee() : BigDecimal.valueOf(100.00))
            .insuranceAccepted(pricingRequest.getInsuranceAccepted() != null ? pricingRequest.getInsuranceAccepted() : false)
            .currency(pricingRequest.getCurrency() != null ? pricingRequest.getCurrency() : "USD")
            .build();
    }
    
    private List<AppointmentSlot> generateAppointmentSlots(ProviderAvailability availability) {
        List<AppointmentSlot> slots = new ArrayList<>();
        LocalTime currentTime = availability.getStartTime();
        
        while (currentTime.plusMinutes(availability.getSlotDuration()).isBefore(availability.getEndTime()) ||
               currentTime.plusMinutes(availability.getSlotDuration()).equals(availability.getEndTime())) {
            
            LocalDateTime slotStart = LocalDateTime.of(availability.getDate(), currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(availability.getSlotDuration());
            
            AppointmentSlot slot = AppointmentSlot.builder()
                .availability(availability)
                .provider(availability.getProvider())
                .slotStartTime(slotStart)
                .slotEndTime(slotEnd)
                .status(AppointmentSlot.SlotStatus.AVAILABLE)
                .appointmentType(availability.getAppointmentType().name())
                .build();
            
            slots.add(slot);
            
            // Add break duration
            currentTime = currentTime.plusMinutes(availability.getSlotDuration() + availability.getBreakDuration());
        }
        
        return slots;
    }
    
    private AvailabilityResponse.DailyAvailability buildDailyAvailability(LocalDate date, List<ProviderAvailability> availabilities) {
        List<AvailabilityResponse.SlotInfo> slots = availabilities.stream()
            .flatMap(availability -> {
                List<AppointmentSlot> appointmentSlots = slotRepository.findAvailableSlotsByAvailabilityId(availability.getId());
                return appointmentSlots.stream().map(this::buildSlotInfo);
            })
            .collect(Collectors.toList());
        
        return AvailabilityResponse.DailyAvailability.builder()
            .date(date)
            .slots(slots)
            .build();
    }
    
    private AvailabilityResponse.SlotInfo buildSlotInfo(AppointmentSlot slot) {
        return AvailabilityResponse.SlotInfo.builder()
            .slotId(slot.getId())
            .startTime(slot.getSlotStartTime().toLocalTime())
            .endTime(slot.getSlotEndTime().toLocalTime())
            .status(slot.getStatus())
            .appointmentType(slot.getAppointmentType())
            .location(buildLocationInfo(slot.getAvailability().getLocation()))
            .pricing(buildPricingInfo(slot.getAvailability().getPricing()))
            .build();
    }
    
    private AvailabilityResponse.LocationInfo buildLocationInfo(AvailabilityLocation location) {
        return AvailabilityResponse.LocationInfo.builder()
            .type(location.getType())
            .address(location.getAddress())
            .roomNumber(location.getRoomNumber())
            .build();
    }
    
    private AvailabilityResponse.PricingInfo buildPricingInfo(AvailabilityPricing pricing) {
        if (pricing == null) {
            return null;
        }
        return AvailabilityResponse.PricingInfo.builder()
            .baseFee(pricing.getBaseFee())
            .insuranceAccepted(pricing.getInsuranceAccepted())
            .currency(pricing.getCurrency())
            .build();
    }
    
    private AvailabilityResponse.AvailabilitySummary calculateAvailabilitySummary(List<ProviderAvailability> availabilities) {
        int totalSlots = 0;
        int availableSlots = 0;
        int bookedSlots = 0;
        int cancelledSlots = 0;
        
        for (ProviderAvailability availability : availabilities) {
            List<AppointmentSlot> slots = slotRepository.findAvailableSlotsByAvailabilityId(availability.getId());
            totalSlots += slots.size();
            
            for (AppointmentSlot slot : slots) {
                switch (slot.getStatus()) {
                    case AVAILABLE:
                        availableSlots++;
                        break;
                    case BOOKED:
                        bookedSlots++;
                        break;
                    case CANCELLED:
                        cancelledSlots++;
                        break;
                }
            }
        }
        
        return AvailabilityResponse.AvailabilitySummary.builder()
            .totalSlots(totalSlots)
            .availableSlots(availableSlots)
            .bookedSlots(bookedSlots)
            .cancelledSlots(cancelledSlots)
            .build();
    }
    
    private AvailabilitySearchResponse.SearchResult buildSearchResult(Provider provider, List<ProviderAvailability> availabilities) {
        List<AvailabilitySearchResponse.AvailableSlot> availableSlots = availabilities.stream()
            .flatMap(availability -> {
                List<AppointmentSlot> slots = slotRepository.findAvailableSlotsByAvailabilityId(availability.getId());
                return slots.stream().map(slot -> buildAvailableSlot(slot, availability));
            })
            .collect(Collectors.toList());
        
        return AvailabilitySearchResponse.SearchResult.builder()
            .provider(buildProviderInfo(provider))
            .availableSlots(availableSlots)
            .build();
    }
    
    private AvailabilitySearchResponse.ProviderInfo buildProviderInfo(Provider provider) {
        return AvailabilitySearchResponse.ProviderInfo.builder()
            .id(provider.getId().toString())
            .name(provider.getFirstName() + " " + provider.getLastName())
            .specialization(provider.getSpecialization())
            .yearsOfExperience(provider.getYearsOfExperience())
            .rating(4.5) // Default rating - could be calculated from reviews
            .clinicAddress(provider.getClinicAddress().getStreet() + ", " + 
                         provider.getClinicAddress().getCity() + ", " + 
                         provider.getClinicAddress().getState())
            .build();
    }
    
    private AvailabilitySearchResponse.AvailableSlot buildAvailableSlot(AppointmentSlot slot, ProviderAvailability availability) {
        return AvailabilitySearchResponse.AvailableSlot.builder()
            .slotId(slot.getId())
            .date(slot.getSlotStartTime().toLocalDate())
            .startTime(slot.getSlotStartTime().toLocalTime())
            .endTime(slot.getSlotEndTime().toLocalTime())
            .appointmentType(slot.getAppointmentType())
            .location(buildSearchLocationInfo(availability.getLocation()))
            .pricing(buildSearchPricingInfo(availability.getPricing()))
            .specialRequirements(availability.getSpecialRequirements())
            .build();
    }
    
    private AvailabilitySearchResponse.LocationInfo buildSearchLocationInfo(AvailabilityLocation location) {
        return AvailabilitySearchResponse.LocationInfo.builder()
            .type(location.getType().name())
            .address(location.getAddress())
            .roomNumber(location.getRoomNumber())
            .build();
    }
    
    private AvailabilitySearchResponse.PricingInfo buildSearchPricingInfo(AvailabilityPricing pricing) {
        if (pricing == null) {
            return null;
        }
        return AvailabilitySearchResponse.PricingInfo.builder()
            .baseFee(pricing.getBaseFee())
            .insuranceAccepted(pricing.getInsuranceAccepted())
            .currency(pricing.getCurrency())
            .build();
    }
    
    private AvailabilitySearchResponse.SearchCriteria buildSearchCriteria(AvailabilitySearchRequest request) {
        return new AvailabilitySearchResponse.SearchCriteria(
            request.getDate(),
            request.getSpecialization(),
            request.getLocation(),
            request.getAppointmentType(),
            request.getInsuranceAccepted(),
            request.getMaxPrice(),
            request.getTimezone()
        );
    }
}
