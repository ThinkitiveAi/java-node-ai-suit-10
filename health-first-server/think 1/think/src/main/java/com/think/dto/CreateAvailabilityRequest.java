package com.think.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.think.entity.ProviderAvailability;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAvailabilityRequest {
    
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotBlank(message = "Timezone is required")
    private String timezone;
    
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    @JsonProperty("slot_duration")
    private Integer slotDuration = 30;
    
    @Min(value = 0, message = "Break duration cannot be negative")
    @Max(value = 120, message = "Break duration cannot exceed 2 hours")
    @JsonProperty("break_duration")
    private Integer breakDuration = 0;
    
    @JsonProperty("is_recurring")
    private Boolean isRecurring = false;
    
    @JsonProperty("recurrence_pattern")
    private ProviderAvailability.RecurrencePattern recurrencePattern;
    
    @JsonProperty("recurrence_end_date")
    private LocalDate recurrenceEndDate;
    
    @JsonProperty("appointment_type")
    private ProviderAvailability.AppointmentType appointmentType = ProviderAvailability.AppointmentType.CONSULTATION;
    
    @Valid
    @NotNull(message = "Location is required")
    private LocationRequest location;
    
    @Valid
    private PricingRequest pricing;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    @JsonProperty("special_requirements")
    private List<String> specialRequirements;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationRequest {
        @NotNull(message = "Location type is required")
        private ProviderAvailability.LocationType type;
        
        @NotBlank(message = "Address is required for physical locations")
        @Size(max = 500, message = "Address cannot exceed 500 characters")
        private String address;
        
        @Size(max = 50, message = "Room number cannot exceed 50 characters")
        @JsonProperty("room_number")
        private String roomNumber;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingRequest {
        @DecimalMin(value = "0.0", inclusive = false, message = "Base fee must be greater than 0")
        @JsonProperty("base_fee")
        private BigDecimal baseFee;
        
        @JsonProperty("insurance_accepted")
        private Boolean insuranceAccepted = false;
        
        @Size(max = 3, message = "Currency code cannot exceed 3 characters")
        private String currency = "USD";
    }
}
