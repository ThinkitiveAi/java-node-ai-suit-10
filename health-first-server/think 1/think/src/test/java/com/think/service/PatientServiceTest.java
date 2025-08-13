package com.think.service;

import com.think.dto.PatientRegistrationRequest;
import com.think.dto.PatientResponse;
import com.think.entity.Patient;
import com.think.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private PatientService patientService;
    
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
    void registerPatient_Success() {
        // Given
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword123");
        
        Patient savedPatient = Patient.builder()
            .id("patient-123")
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@email.com")
            .phoneNumber("+1234567890")
            .passwordHash("hashedPassword123")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .gender(Patient.Gender.FEMALE)
            .emailVerified(false)
            .phoneVerified(false)
            .isActive(true)
            .build();
        
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        
        // When
        PatientResponse response = patientService.registerPatient(validRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("patient-123", response.getPatientId());
        assertEquals("jane.smith@email.com", response.getEmail());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertFalse(response.getEmailVerified());
        assertFalse(response.getPhoneVerified());
        
        verify(patientRepository).existsByEmail("jane.smith@email.com");
        verify(patientRepository).existsByPhoneNumber("+1234567890");
        verify(passwordEncoder).encode("SecurePassword123!");
        verify(patientRepository).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_PasswordMismatch() {
        // Given
        validRequest.setConfirmPassword("DifferentPassword123!");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest));
        
        assertEquals("Password and confirmation password do not match", exception.getMessage());
        
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_UnderagePatient() {
        // Given
        validRequest.setDateOfBirth(LocalDate.now().minusYears(10)); // 10 years old
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest));
        
        assertEquals("Patient must be at least 13 years old for COPPA compliance", exception.getMessage());
        
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_DuplicateEmail() {
        // Given
        when(patientRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest));
        
        assertEquals("Email is already registered", exception.getMessage());
        
        verify(patientRepository).existsByEmail("jane.smith@email.com");
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_DuplicatePhoneNumber() {
        // Given
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(anyString())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest));
        
        assertEquals("Phone number is already registered", exception.getMessage());
        
        verify(patientRepository).existsByEmail("jane.smith@email.com");
        verify(patientRepository).existsByPhoneNumber("+1234567890");
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_PasswordHashing() {
        // Given
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPasswordWithSalt");
        
        Patient savedPatient = Patient.builder()
            .id("patient-123")
            .passwordHash("$2a$12$hashedPasswordWithSalt")
            .build();
        
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        
        // When
        patientService.registerPatient(validRequest);
        
        // Then
        verify(passwordEncoder).encode("SecurePassword123!");
        verify(patientRepository).save(argThat(patient -> 
            patient.getPasswordHash().equals("$2a$12$hashedPasswordWithSalt")
        ));
    }
    
    @Test
    void registerPatient_DataSanitization() {
        // Given
        validRequest.setFirstName("  Jane  ");
        validRequest.setLastName("  Smith  ");
        validRequest.setEmail("  JANE.SMITH@EMAIL.COM  ");
        validRequest.setPhoneNumber("  +1234567890  ");
        
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        
        Patient savedPatient = Patient.builder().id("patient-123").build();
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        
        // When
        patientService.registerPatient(validRequest);
        
        // Then
        verify(patientRepository).existsByEmail("jane.smith@email.com");
        verify(patientRepository).existsByPhoneNumber("+1234567890");
        verify(patientRepository).save(argThat(patient -> 
            patient.getFirstName().equals("Jane") &&
            patient.getLastName().equals("Smith") &&
            patient.getEmail().equals("jane.smith@email.com") &&
            patient.getPhoneNumber().equals("+1234567890")
        ));
    }
    
    @Test
    void registerPatient_OptionalFieldsNull() {
        // Given
        validRequest.setEmergencyContact(null);
        validRequest.setMedicalHistory(null);
        validRequest.setInsuranceInfo(null);
        
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        
        Patient savedPatient = Patient.builder().id("patient-123").build();
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        
        // When
        PatientResponse response = patientService.registerPatient(validRequest);
        
        // Then
        assertNotNull(response);
        verify(patientRepository).save(argThat(patient -> 
            patient.getEmergencyContact() == null &&
            patient.getMedicalHistory() == null &&
            patient.getInsuranceInfo() == null
        ));
    }
}
