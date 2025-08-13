package com.think.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.think.dto.ProviderLoginRequest;
import com.think.dto.ProviderLoginResponse;
import com.think.entity.ClinicAddress;
import com.think.entity.Provider;
import com.think.service.ProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProviderService providerService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ProviderLoginRequest validRequest;
    private ProviderLoginResponse validResponse;
    private Provider savedProvider;
    
    @BeforeEach
    void setUp() {
        validRequest = new ProviderLoginRequest();
        validRequest.setEmail("john.doe@clinic.com");
        validRequest.setPassword("SecurePassword123!");
        
        // Setup saved provider
        savedProvider = new Provider();
        savedProvider.setId(UUID.randomUUID());
        savedProvider.setFirstName("John");
        savedProvider.setLastName("Doe");
        savedProvider.setEmail("john.doe@clinic.com");
        savedProvider.setPhoneNumber("+1234567890");
        savedProvider.setPasswordHash("hashedPassword");
        savedProvider.setSpecialization("Cardiology");
        savedProvider.setLicenseNumber("MD123456");
        savedProvider.setYearsOfExperience(10);
        savedProvider.setVerificationStatus(Provider.VerificationStatus.PENDING);
        savedProvider.setIsActive(true);
        savedProvider.setCreatedAt(LocalDateTime.now());
        savedProvider.setUpdatedAt(LocalDateTime.now());
        
        ClinicAddress clinicAddress = new ClinicAddress();
        clinicAddress.setStreet("123 Medical Center Dr");
        clinicAddress.setCity("New York");
        clinicAddress.setState("NY");
        clinicAddress.setZip("10001");
        savedProvider.setClinicAddress(clinicAddress);
        
        validResponse = ProviderLoginResponse.success("jwt-token-here", 3600L, 
                com.think.dto.ProviderResponse.fromProvider(savedProvider));
    }
    
    @Test
    void login_Success() throws Exception {
        // Given
        when(providerService.loginProvider(any(ProviderLoginRequest.class))).thenReturn(validResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/provider/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.access_token").value("jwt-token-here"))
                .andExpect(jsonPath("$.data.expires_in").value(3600))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"))
                .andExpect(jsonPath("$.data.provider.email").value("john.doe@clinic.com"))
                .andExpect(jsonPath("$.data.provider.firstName").value("John"))
                .andExpect(jsonPath("$.data.provider.lastName").value("Doe"));
    }
    
    @Test
    void login_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setEmail("invalid-email");
        
        // When & Then
        mockMvc.perform(post("/api/v1/provider/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email must be a valid email address"));
    }
    
    @Test
    void login_MissingEmail_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setEmail("");
        
        // When & Then
        mockMvc.perform(post("/api/v1/provider/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email is required"));
    }
    
    @Test
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setPassword("");
        
        // When & Then
        mockMvc.perform(post("/api/v1/provider/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is required"));
    }
    
    @Test
    void login_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Given
        when(providerService.loginProvider(any(ProviderLoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/provider/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
    
    @Test
    void login_InactiveAccount_ReturnsBadRequest() throws Exception {
        // Given
        when(providerService.loginProvider(any(ProviderLoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Account is deactivated. Please contact support."));
        
        // When & Then
        mockMvc.perform(post("/api/v1/provider/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Account is deactivated. Please contact support."));
    }
}
