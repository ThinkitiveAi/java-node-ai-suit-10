package com.think.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.think.dto.ProviderRegistrationRequest;
import com.think.dto.ProviderResponse;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProviderService providerService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ProviderRegistrationRequest validRequest;
    private ProviderResponse validResponse;
    private Provider savedProvider;
    
    @BeforeEach
    void setUp() {
        validRequest = new ProviderRegistrationRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhoneNumber("+1234567890");
        validRequest.setPassword("SecurePass123!");
        validRequest.setSpecialization("Cardiology");
        validRequest.setLicenseNumber("MD123456");
        validRequest.setYearsOfExperience(10);
        
        ProviderRegistrationRequest.ClinicAddressRequest addressRequest = new ProviderRegistrationRequest.ClinicAddressRequest();
        addressRequest.setStreet("123 Medical Center Dr");
        addressRequest.setCity("New York");
        addressRequest.setState("NY");
        addressRequest.setZip("10001");
        validRequest.setClinicAddress(addressRequest);
        
        // Setup saved provider
        savedProvider = new Provider();
        savedProvider.setId(UUID.randomUUID());
        savedProvider.setFirstName("John");
        savedProvider.setLastName("Doe");
        savedProvider.setEmail("john.doe@example.com");
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
        
        validResponse = ProviderResponse.fromProvider(savedProvider);
    }
    
    @Test
    void registerProvider_Success() throws Exception {
        // Given
        when(providerService.registerProvider(any(ProviderRegistrationRequest.class))).thenReturn(validResponse);
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedProvider.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.licenseNumber").value("MD123456"))
                .andExpect(jsonPath("$.yearsOfExperience").value(10))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.clinicAddress.street").value("123 Medical Center Dr"))
                .andExpect(jsonPath("$.clinicAddress.city").value("New York"))
                .andExpect(jsonPath("$.clinicAddress.state").value("NY"))
                .andExpect(jsonPath("$.clinicAddress.zip").value("10001"));
    }
    
    @Test
    void registerProvider_DuplicateEmail_ReturnsBadRequest() throws Exception {
        // Given
        when(providerService.registerProvider(any(ProviderRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered: john.doe@example.com"));
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered: john.doe@example.com"));
    }
    
    @Test
    void registerProvider_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setEmail("invalid-email");
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email must be a valid email address"));
    }
    
    @Test
    void registerProvider_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setPhoneNumber("1234567890"); // Missing + prefix
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone number must be in international format (e.g., +1234567890)"));
    }
    
    @Test
    void registerProvider_WeakPassword_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setPassword("weak");
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"));
    }
    
    @Test
    void registerProvider_InvalidLicenseNumber_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setLicenseNumber("MD-123-456"); // Contains special characters
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.licenseNumber").value("License number must be alphanumeric"));
    }
    
    @Test
    void registerProvider_InvalidZipCode_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.getClinicAddress().setZip("invalid");
        
        // When & Then
        mockMvc.perform(post("/api/providers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.clinicAddress.zip").value("ZIP code must be in valid format (e.g., 12345 or 12345-6789)"));
    }
    
    @Test
    void getProviderById_Success() throws Exception {
        // Given
        UUID providerId = UUID.randomUUID();
        when(providerService.getProviderById(providerId)).thenReturn(Optional.of(validResponse));
        
        // When & Then
        mockMvc.perform(get("/api/providers/" + providerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProvider.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
    
    @Test
    void getProviderById_NotFound() throws Exception {
        // Given
        UUID providerId = UUID.randomUUID();
        when(providerService.getProviderById(providerId)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/providers/" + providerId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void getProviderByEmail_Success() throws Exception {
        // Given
        String email = "john.doe@example.com";
        when(providerService.getProviderByEmail(email)).thenReturn(Optional.of(validResponse));
        
        // When & Then
        mockMvc.perform(get("/api/providers/email/" + email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
    
    @Test
    void getProviderByEmail_NotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        when(providerService.getProviderByEmail(email)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/providers/email/" + email))
                .andExpect(status().isNotFound());
    }
}
