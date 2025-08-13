package com.think.dto;

import com.think.entity.Patient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    
    private String patientId;
    private String email;
    private String phoneNumber;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    
    public static PatientResponse fromPatient(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getEmail(),
            patient.getPhoneNumber(),
            patient.getEmailVerified(),
            patient.getPhoneVerified()
        );
    }
}
