package com.think.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientLoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
}
