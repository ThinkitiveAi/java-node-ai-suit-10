package com.think.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.think.entity.AppointmentSlot;
import com.think.entity.ProviderAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {
    
    @JsonProperty("availability_id")
    private String availabilityId;
    
    @JsonProperty("slots_created")
    private Integer slotsCreated;
    
    @JsonProperty("date_range")
    private DateRange dateRange;
    
    @JsonProperty("total_appointments_available")
    private Integer totalAppointmentsAvailable;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        private LocalDate start;
        private LocalDate end;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilitySummary {
        @JsonProperty("total_slots")
        private Integer totalSlots;
        
        @JsonProperty("available_slots")
        private Integer availableSlots;
        
        @JsonProperty("booked_slots")
        private Integer bookedSlots;
        
        @JsonProperty("cancelled_slots")
        private Integer cancelledSlots;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotInfo {
        @JsonProperty("slot_id")
        private String slotId;
        
        @JsonProperty("start_time")
        private LocalTime startTime;
        
        @JsonProperty("end_time")
        private LocalTime endTime;
        
        private AppointmentSlot.SlotStatus status;
        
        @JsonProperty("appointment_type")
        private String appointmentType;
        
        private LocationInfo location;
        
        private PricingInfo pricing;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationInfo {
        private ProviderAvailability.LocationType type;
        private String address;
        
        @JsonProperty("room_number")
        private String roomNumber;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PricingInfo {
        @JsonProperty("base_fee")
        private BigDecimal baseFee;
        
        @JsonProperty("insurance_accepted")
        private Boolean insuranceAccepted;
        
        private String currency;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyAvailability {
        private LocalDate date;
        private List<SlotInfo> slots;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProviderAvailabilityResponse {
        @JsonProperty("provider_id")
        private String providerId;
        
        @JsonProperty("availability_summary")
        private AvailabilitySummary availabilitySummary;
        
        private List<DailyAvailability> availability;
    }
}
