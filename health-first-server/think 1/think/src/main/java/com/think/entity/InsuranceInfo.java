package com.think.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceInfo {
    
    @Size(max = 100, message = "Insurance provider cannot exceed 100 characters")
    private String provider;
    
    @Size(max = 50, message = "Policy number cannot exceed 50 characters")
    private String policyNumber;
}
