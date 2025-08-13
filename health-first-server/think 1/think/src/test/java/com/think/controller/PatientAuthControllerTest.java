package com.think.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.think.dto.PatientLoginRequest;
import com.think.dto.PatientLoginResponse;
import com.think.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientAuthController.class)
class PatientAuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PatientService patientService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void loginPatient_WithValidCredentials_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("jane.smith@email.com", "SecurePassword123!");
        
        PatientLoginResponse.PatientData patientData = PatientLoginResponse.PatientData.builder()
            .patientId("patient-123")
            .email("jane.smith@email.com")
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+1234567890")
            .emailVerified(true)
            .phoneVerified(false)
            .isActive(true)
            .build();
        
        PatientLoginResponse loginResponse = PatientLoginResponse.builder()
            .accessToken("jwt-token-here")
            .expiresIn(1800L)
            .tokenType("Bearer")
            .patient(patientData)
            .build();
        
        when(patientService.loginPatient(any(PatientLoginRequest.class)))
            .thenReturn(loginResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.data.access_token").value("jwt-token-here"))
            .andExpect(jsonPath("$.data.expires_in").value(1800))
            .andExpect(jsonPath("$.data.token_type").value("Bearer"))
            .andExpect(jsonPath("$.data.patient.patient_id").value("patient-123"))
            .andExpect(jsonPath("$.data.patient.email").value("jane.smith@email.com"))
            .andExpect(jsonPath("$.data.patient.first_name").value("Jane"))
            .andExpect(jsonPath("$.data.patient.last_name").value("Smith"))
            .andExpect(jsonPath("$.data.patient.phone_number").value("+1234567890"))
            .andExpect(jsonPath("$.data.patient.email_verified").value(true))
            .andExpect(jsonPath("$.data.patient.phone_verified").value(false))
            .andExpect(jsonPath("$.data.patient.is_active").value(true));
    }
    
    @Test
    void loginPatient_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("invalid@email.com", "wrongpassword");
        
        when(patientService.loginPatient(any(PatientLoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email or password"));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
    
    @Test
    void loginPatient_WithInactiveAccount_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("inactive@email.com", "SecurePassword123!");
        
        when(patientService.loginPatient(any(PatientLoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Account is deactivated. Please contact support."));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Account is deactivated. Please contact support."));
    }
    
    @Test
    void loginPatient_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("invalid-email", "SecurePassword123!");
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginPatient_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String requestJson = "{\"password\": \"SecurePassword123!\"}";
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginPatient_WithMissingPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String requestJson = "{\"email\": \"jane.smith@email.com\"}";
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginPatient_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("", "SecurePassword123!");
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginPatient_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("jane.smith@email.com", "");
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginPatient_WithUnexpectedError_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest("jane.smith@email.com", "SecurePassword123!");
        
        when(patientService.loginPatient(any(PatientLoginRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
    }
}
