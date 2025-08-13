package com.think.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {
    
    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    private String name;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Emergency contact phone must be in valid international format")
    private String phone;
    
    @Size(max = 50, message = "Relationship cannot exceed 50 characters")
    private String relationship;
}
