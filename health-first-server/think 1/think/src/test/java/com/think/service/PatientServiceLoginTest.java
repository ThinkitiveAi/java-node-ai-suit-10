package com.think.service;

import com.think.dto.PatientLoginRequest;
import com.think.dto.PatientLoginResponse;
import com.think.entity.*;
import com.think.repository.PatientRepository;
import com.think.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceLoginTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private PatientService patientService;
    
    private Patient testPatient;
    private PatientLoginRequest validLoginRequest;
    private String validPassword = "SecurePassword123!";
    private String hashedPassword = "$2a$12$hashedPasswordHash";
    
    @BeforeEach
    void setUp() {
        // Create test patient
        testPatient = Patient.builder()
            .id("patient-123")
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@email.com")
            .phoneNumber("+1234567890")
            .passwordHash(hashedPassword)
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .gender(Patient.Gender.FEMALE)
            .address(new PatientAddress("456 Main Street", "Boston", "MA", "02101"))
            .emailVerified(true)
            .phoneVerified(false)
            .isActive(true)
            .build();
        
        // Create valid login request
        validLoginRequest = new PatientLoginRequest("jane.smith@email.com", validPassword);
    }
    
    @Test
    void loginPatient_WithValidCredentials_ShouldReturnLoginResponse() {
        // Arrange
        when(patientRepository.findByEmail("jane.smith@email.com"))
            .thenReturn(Optional.of(testPatient));
        when(passwordEncoder.matches(validPassword, hashedPassword))
            .thenReturn(true);
        when(jwtUtil.generateToken(anyMap(), eq("jane.smith@email.com"), eq(1800L)))
            .thenReturn("jwt-token-here");
        
        // Act
        PatientLoginResponse response = patientService.loginPatient(validLoginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-here", response.getAccessToken());
        assertEquals(1800L, response.getExpiresIn());
        assertEquals("Bearer", response.getTokenType());
        
        PatientLoginResponse.PatientData patientData = response.getPatient();
        assertNotNull(patientData);
        assertEquals("patient-123", patientData.getPatientId());
        assertEquals("jane.smith@email.com", patientData.getEmail());
        assertEquals("Jane", patientData.getFirstName());
        assertEquals("Smith", patientData.getLastName());
        assertEquals("+1234567890", patientData.getPhoneNumber());
        assertTrue(patientData.getEmailVerified());
        assertFalse(patientData.getPhoneVerified());
        assertTrue(patientData.getIsActive());
        
        // Verify JWT token generation with correct claims
        Map<String, Object> expectedClaims = new HashMap<>();
        expectedClaims.put("patient_id", "patient-123");
        expectedClaims.put("email", "jane.smith@email.com");
        expectedClaims.put("role", "PATIENT");
        
        verify(jwtUtil).generateToken(expectedClaims, "jane.smith@email.com", 1800L);
    }
    
    @Test
    void loginPatient_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        when(patientRepository.findByEmail("invalid@email.com"))
            .thenReturn(Optional.empty());
        
        PatientLoginRequest invalidRequest = new PatientLoginRequest("invalid@email.com", validPassword);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.loginPatient(invalidRequest)
        );
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyMap(), anyString(), anyLong());
    }
    
    @Test
    void loginPatient_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        when(patientRepository.findByEmail("jane.smith@email.com"))
            .thenReturn(Optional.of(testPatient));
        when(passwordEncoder.matches("wrongpassword", hashedPassword))
            .thenReturn(false);
        
        PatientLoginRequest invalidRequest = new PatientLoginRequest("jane.smith@email.com", "wrongpassword");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.loginPatient(invalidRequest)
        );
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyMap(), anyString(), anyLong());
    }
    
    @Test
    void loginPatient_WithInactiveAccount_ShouldThrowException() {
        // Arrange
        testPatient.setIsActive(false);
        when(patientRepository.findByEmail("jane.smith@email.com"))
            .thenReturn(Optional.of(testPatient));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.loginPatient(validLoginRequest)
        );
        
        assertEquals("Account is deactivated. Please contact support.", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyMap(), anyString(), anyLong());
    }
    
    @Test
    void loginPatient_WithEmailCaseInsensitive_ShouldWork() {
        // Arrange
        when(patientRepository.findByEmail("jane.smith@email.com"))
            .thenReturn(Optional.of(testPatient));
        when(passwordEncoder.matches(validPassword, hashedPassword))
            .thenReturn(true);
        when(jwtUtil.generateToken(anyMap(), eq("jane.smith@email.com"), eq(1800L)))
            .thenReturn("jwt-token-here");
        
        PatientLoginRequest uppercaseRequest = new PatientLoginRequest("JANE.SMITH@EMAIL.COM", validPassword);
        
        // Act
        PatientLoginResponse response = patientService.loginPatient(uppercaseRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-here", response.getAccessToken());
        
        // Verify email was converted to lowercase
        verify(patientRepository).findByEmail("jane.smith@email.com");
    }
    
    @Test
    void loginPatient_WithEmailWhitespace_ShouldWork() {
        // Arrange
        when(patientRepository.findByEmail("jane.smith@email.com"))
            .thenReturn(Optional.of(testPatient));
        when(passwordEncoder.matches(validPassword, hashedPassword))
            .thenReturn(true);
        when(jwtUtil.generateToken(anyMap(), eq("jane.smith@email.com"), eq(1800L)))
            .thenReturn("jwt-token-here");
        
        PatientLoginRequest whitespaceRequest = new PatientLoginRequest("  jane.smith@email.com  ", validPassword);
        
        // Act
        PatientLoginResponse response = patientService.loginPatient(whitespaceRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-here", response.getAccessToken());
        
        // Verify email was trimmed
        verify(patientRepository).findByEmail("jane.smith@email.com");
    }
}
