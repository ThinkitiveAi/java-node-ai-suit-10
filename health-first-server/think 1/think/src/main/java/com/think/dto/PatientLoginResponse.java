package com.think.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientLoginResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    private PatientData patient;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatientData {
        @JsonProperty("patient_id")
        private String patientId;
        
        private String email;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        @JsonProperty("phone_number")
        private String phoneNumber;
        
        @JsonProperty("email_verified")
        private Boolean emailVerified;
        
        @JsonProperty("phone_verified")
        private Boolean phoneVerified;
        
        @JsonProperty("is_active")
        private Boolean isActive;
    }
}
