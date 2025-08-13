package com.think.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityPricing {
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Base fee must be greater than 0")
    @Column(name = "base_fee", precision = 10, scale = 2)
    private BigDecimal baseFee;
    
    @Column(name = "insurance_accepted")
    private Boolean insuranceAccepted = false;
    
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    @Column(name = "currency", length = 3)
    private String currency = "USD";
}
