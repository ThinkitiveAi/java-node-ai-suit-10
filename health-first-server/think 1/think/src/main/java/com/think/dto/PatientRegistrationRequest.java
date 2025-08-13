package com.think.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.think.entity.Patient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @JsonProperty("first_name")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @JsonProperty("last_name")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format (+1234567890)")
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least 8 characters including uppercase, lowercase, number, and special character"
    )
    private String password;
    
    @NotBlank(message = "Password confirmation is required")
    @JsonProperty("confirm_password")
    private String confirmPassword;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    
    @NotNull(message = "Gender is required")
    private Patient.Gender gender;
    
    @Valid
    @NotNull(message = "Address is required")
    private PatientAddressRequest address;
    
    @JsonProperty("emergency_contact")
    private EmergencyContactRequest emergencyContact;
    
    @JsonProperty("medical_history")
    private List<String> medicalHistory;
    
    @JsonProperty("insurance_info")
    private InsuranceInfoRequest insuranceInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientAddressRequest {
        @NotBlank(message = "Street is required")
        @Size(max = 200, message = "Street must not exceed 200 characters")
        private String street;
        
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;
        
        @NotBlank(message = "State is required")
        @Size(max = 50, message = "State must not exceed 50 characters")
        private String state;
        
        @NotBlank(message = "ZIP code is required")
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "ZIP code must be in valid format (12345 or 12345-6789)")
        private String zip;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyContactRequest {
        @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
        private String name;
        
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Emergency contact phone must be in international format")
        private String phone;
        
        @Size(max = 50, message = "Relationship must not exceed 50 characters")
        private String relationship;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuranceInfoRequest {
        private String provider;
        
        @JsonProperty("policy_number")
        private String policyNumber;
    }
}
