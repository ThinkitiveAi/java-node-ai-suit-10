package com.think.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AvailabilitySearchResponse {
    
    @JsonProperty("search_criteria")
    private SearchCriteria searchCriteria;
    
    @JsonProperty("total_results")
    private Integer totalResults;
    
    private List<SearchResult> results;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchCriteria {
        private LocalDate date;
        private String specialization;
        private String location;
        private String appointmentType;
        private Boolean insuranceAccepted;
        private BigDecimal maxPrice;
        private String timezone;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchResult {
        private ProviderInfo provider;
        
        @JsonProperty("available_slots")
        private List<AvailableSlot> availableSlots;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProviderInfo {
        private String id;
        private String name;
        private String specialization;
        
        @JsonProperty("years_of_experience")
        private Integer yearsOfExperience;
        
        private Double rating;
        
        @JsonProperty("clinic_address")
        private String clinicAddress;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableSlot {
        @JsonProperty("slot_id")
        private String slotId;
        
        private LocalDate date;
        
        @JsonProperty("start_time")
        private LocalTime startTime;
        
        @JsonProperty("end_time")
        private LocalTime endTime;
        
        @JsonProperty("appointment_type")
        private String appointmentType;
        
        private LocationInfo location;
        
        private PricingInfo pricing;
        
        @JsonProperty("special_requirements")
        private List<String> specialRequirements;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationInfo {
        private String type;
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
}
