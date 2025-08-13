package com.think.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AvailabilitySearchRequest {
    
    private LocalDate date;
    
    @JsonProperty("start_date")
    private LocalDate startDate;
    
    @JsonProperty("end_date")
    private LocalDate endDate;
    
    private String specialization;
    
    private String location;
    
    @JsonProperty("appointment_type")
    private String appointmentType;
    
    @JsonProperty("insurance_accepted")
    private Boolean insuranceAccepted;
    
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    
    private String timezone;
    
    @JsonProperty("available_only")
    private Boolean availableOnly = true;
}
