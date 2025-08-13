package com.think.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    
    private String appointmentId;
    private String bookingReference;
    private UUID patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private UUID providerId;
    private String providerName;
    private String providerSpecialization;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private LocalDateTime appointmentDateTime;
    private String appointmentType;
    private String appointmentMode;
    private String reasonForVisit;
    private String additionalNotes;
    private String status;
    private BigDecimal estimatedCost;
    private String currency;
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Patient demographics
    private String patientGender;
    private LocalDate patientDateOfBirth;
    private String patientAddress;
    
    // Provider details
    private String providerEmail;
    private String providerPhone;
    private String clinicAddress;
}
