package com.think.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    
    @NotNull(message = "Patient ID is required")
    private String patientId;
    
    @NotNull(message = "Provider ID is required")
    private String providerId;
    
    @NotNull(message = "Appointment date is required")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;
    
    @NotBlank(message = "Appointment type is required")
    @Pattern(regexp = "^(CONSULTATION|FOLLOW_UP|EMERGENCY|TELEMEDICINE)$", 
             message = "Appointment type must be one of: CONSULTATION, FOLLOW_UP, EMERGENCY, TELEMEDICINE")
    private String appointmentType;
    
    @NotBlank(message = "Appointment mode is required")
    @Pattern(regexp = "^(IN_PERSON|TELEMEDICINE|HOME_VISIT)$", 
             message = "Appointment mode must be one of: IN_PERSON, TELEMEDICINE, HOME_VISIT")
    private String appointmentMode;
    
    @NotBlank(message = "Reason for visit is required")
    @Size(min = 10, max = 500, message = "Reason for visit must be between 10 and 500 characters")
    private String reasonForVisit;
    
    @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
    private String additionalNotes;
    
    @Pattern(regexp = "^[A-Za-z0-9\\s\\-]+$", message = "Insurance provider can only contain letters, numbers, spaces, and hyphens")
    @Size(max = 100, message = "Insurance provider cannot exceed 100 characters")
    private String insuranceProvider;
    
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "Insurance policy number can only contain letters, numbers, and hyphens")
    @Size(max = 50, message = "Insurance policy number cannot exceed 50 characters")
    private String insurancePolicyNumber;
}
