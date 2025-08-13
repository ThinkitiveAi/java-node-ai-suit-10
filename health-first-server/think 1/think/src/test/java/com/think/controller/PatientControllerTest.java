package com.think.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.think.dto.PatientRegistrationRequest;
import com.think.dto.PatientResponse;
import com.think.entity.Patient;
import com.think.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PatientService patientService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PatientRegistrationRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = new PatientRegistrationRequest();
        validRequest.setFirstName("Jane");
        validRequest.setLastName("Smith");
        validRequest.setEmail("jane.smith@email.com");
        validRequest.setPhoneNumber("+1234567890");
        validRequest.setPassword("SecurePassword123!");
        validRequest.setConfirmPassword("SecurePassword123!");
        validRequest.setDateOfBirth(LocalDate.of(1990, 5, 15));
        validRequest.setGender(Patient.Gender.FEMALE);
        
        PatientRegistrationRequest.PatientAddressRequest address = new PatientRegistrationRequest.PatientAddressRequest();
        address.setStreet("456 Main Street");
        address.setCity("Boston");
        address.setState("MA");
        address.setZip("02101");
        validRequest.setAddress(address);
        
        PatientRegistrationRequest.EmergencyContactRequest emergencyContact = new PatientRegistrationRequest.EmergencyContactRequest();
        emergencyContact.setName("John Smith");
        emergencyContact.setPhone("+1234567891");
        emergencyContact.setRelationship("spouse");
        validRequest.setEmergencyContact(emergencyContact);
        
        validRequest.setMedicalHistory(Arrays.asList("Hypertension", "Diabetes"));
        
        PatientRegistrationRequest.InsuranceInfoRequest insuranceInfo = new PatientRegistrationRequest.InsuranceInfoRequest();
        insuranceInfo.setProvider("Blue Cross");
        insuranceInfo.setPolicyNumber("BC123456789");
        validRequest.setInsuranceInfo(insuranceInfo);
    }
    
    @Test
    void registerPatient_Success() throws Exception {
        // Given
        PatientResponse patientResponse = new PatientResponse(
            "patient-123",
            "jane.smith@email.com",
            "+1234567890",
            false,
            false
        );
        
        when(patientService.registerPatient(any(PatientRegistrationRequest.class)))
            .thenReturn(patientResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Patient registered successfully. Verification email sent."))
            .andExpect(jsonPath("$.data.patientId").value("patient-123"))
            .andExpect(jsonPath("$.data.email").value("jane.smith@email.com"))
            .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
            .andExpect(jsonPath("$.data.emailVerified").value(false))
            .andExpect(jsonPath("$.data.phoneVerified").value(false));
    }
    
    @Test
    void registerPatient_ValidationError_MissingFirstName() throws Exception {
        // Given
        validRequest.setFirstName("");
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.firstName").value("First name is required"));
    }
    
    @Test
    void registerPatient_ValidationError_InvalidEmail() throws Exception {
        // Given
        validRequest.setEmail("invalid-email");
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.email").value("Email must be in valid format"));
    }
    
    @Test
    void registerPatient_ValidationError_InvalidPhoneNumber() throws Exception {
        // Given
        validRequest.setPhoneNumber("1234567890"); // Missing + prefix
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.phoneNumber").value("Phone number must be in valid international format"));
    }
    
    @Test
    void registerPatient_ValidationError_WeakPassword() throws Exception {
        // Given
        validRequest.setPassword("weak");
        validRequest.setConfirmPassword("weak");
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.password").value("Password must contain at least 8 characters, including uppercase, lowercase, number, and special character"));
    }
    
    @Test
    void registerPatient_ValidationError_InvalidZipCode() throws Exception {
        // Given
        validRequest.getAddress().setZip("invalid");
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.address.zip").value("ZIP code must be in valid format (e.g., 12345 or 12345-6789)"));
    }
    
    @Test
    void registerPatient_ValidationError_FutureDateOfBirth() throws Exception {
        // Given
        validRequest.setDateOfBirth(LocalDate.now().plusYears(1));
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.dateOfBirth").value("Date of birth must be in the past"));
    }
    
    @Test
    void registerPatient_ServiceError_DuplicateEmail() throws Exception {
        // Given
        when(patientService.registerPatient(any(PatientRegistrationRequest.class)))
            .thenThrow(new IllegalArgumentException("Email is already registered"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email is already registered"));
    }
    
    @Test
    void registerPatient_ServiceError_UnderagePatient() throws Exception {
        // Given
        when(patientService.registerPatient(any(PatientRegistrationRequest.class)))
            .thenThrow(new IllegalArgumentException("Patient must be at least 13 years old for COPPA compliance"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Patient must be at least 13 years old for COPPA compliance"));
    }
    
    @Test
    void registerPatient_ServiceError_PasswordMismatch() throws Exception {
        // Given
        when(patientService.registerPatient(any(PatientRegistrationRequest.class)))
            .thenThrow(new IllegalArgumentException("Password and confirmation password do not match"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Password and confirmation password do not match"));
    }
}
