package com.think.service;

import com.think.dto.ProviderLoginRequest;
import com.think.dto.ProviderLoginResponse;
import com.think.entity.ClinicAddress;
import com.think.entity.Provider;
import com.think.repository.ProviderRepository;
import com.think.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceLoginTest {
    
    @Mock
    private ProviderRepository providerRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private ProviderService providerService;
    
    private ProviderLoginRequest validLoginRequest;
    private Provider existingProvider;
    
    @BeforeEach
    void setUp() {
        validLoginRequest = new ProviderLoginRequest();
        validLoginRequest.setEmail("john.doe@clinic.com");
        validLoginRequest.setPassword("SecurePassword123!");
        
        // Setup existing provider
        existingProvider = new Provider();
        existingProvider.setId(UUID.randomUUID());
        existingProvider.setFirstName("John");
        existingProvider.setLastName("Doe");
        existingProvider.setEmail("john.doe@clinic.com");
        existingProvider.setPhoneNumber("+1234567890");
        existingProvider.setPasswordHash("hashedPassword");
        existingProvider.setSpecialization("Cardiology");
        existingProvider.setLicenseNumber("MD123456");
        existingProvider.setYearsOfExperience(10);
        existingProvider.setVerificationStatus(Provider.VerificationStatus.PENDING);
        existingProvider.setIsActive(true);
        existingProvider.setCreatedAt(LocalDateTime.now());
        existingProvider.setUpdatedAt(LocalDateTime.now());
        
        ClinicAddress clinicAddress = new ClinicAddress();
        clinicAddress.setStreet("123 Medical Center Dr");
        clinicAddress.setCity("New York");
        clinicAddress.setState("NY");
        clinicAddress.setZip("10001");
        existingProvider.setClinicAddress(clinicAddress);
    }
    
    @Test
    void loginProvider_Success() {
        // Given
        when(providerRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(existingProvider));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), existingProvider.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(existingProvider.getId(), existingProvider.getEmail(), existingProvider.getSpecialization()))
                .thenReturn("jwt-token-here");
        when(jwtUtil.getExpirationTime()).thenReturn(3600L);
        
        // When
        ProviderLoginResponse result = providerService.loginProvider(validLoginRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Login successful", result.getMessage());
        assertNotNull(result.getData());
        assertEquals("jwt-token-here", result.getData().getAccess_token());
        assertEquals(3600L, result.getData().getExpires_in());
        assertEquals("Bearer", result.getData().getToken_type());
        assertNotNull(result.getData().getProvider());
        assertEquals(existingProvider.getEmail(), result.getData().getProvider().getEmail());
        
        verify(providerRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), existingProvider.getPasswordHash());
        verify(jwtUtil).generateToken(existingProvider.getId(), existingProvider.getEmail(), existingProvider.getSpecialization());
        verify(jwtUtil).getExpirationTime();
    }
    
    @Test
    void loginProvider_ProviderNotFound_ThrowsException() {
        // Given
        when(providerRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.loginProvider(validLoginRequest);
        });
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(providerRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }
    
    @Test
    void loginProvider_InactiveProvider_ThrowsException() {
        // Given
        existingProvider.setIsActive(false);
        when(providerRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(existingProvider));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.loginProvider(validLoginRequest);
        });
        
        assertEquals("Account is deactivated. Please contact support.", exception.getMessage());
        verify(providerRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }
    
    @Test
    void loginProvider_InvalidPassword_ThrowsException() {
        // Given
        when(providerRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(existingProvider));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), existingProvider.getPasswordHash())).thenReturn(false);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.loginProvider(validLoginRequest);
        });
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(providerRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), existingProvider.getPasswordHash());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }
    
    @Test
    void loginProvider_EmailCaseInsensitive() {
        // Given
        validLoginRequest.setEmail("JOHN.DOE@CLINIC.COM");
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(existingProvider));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), existingProvider.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(existingProvider.getId(), existingProvider.getEmail(), existingProvider.getSpecialization()))
                .thenReturn("jwt-token-here");
        when(jwtUtil.getExpirationTime()).thenReturn(3600L);
        
        // When
        ProviderLoginResponse result = providerService.loginProvider(validLoginRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(providerRepository).findByEmail("john.doe@clinic.com");
    }
    
    @Test
    void loginProvider_EmailWithWhitespace() {
        // Given
        validLoginRequest.setEmail("  john.doe@clinic.com  ");
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(existingProvider));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), existingProvider.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(existingProvider.getId(), existingProvider.getEmail(), existingProvider.getSpecialization()))
                .thenReturn("jwt-token-here");
        when(jwtUtil.getExpirationTime()).thenReturn(3600L);
        
        // When
        ProviderLoginResponse result = providerService.loginProvider(validLoginRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(providerRepository).findByEmail("john.doe@clinic.com");
    }
}
