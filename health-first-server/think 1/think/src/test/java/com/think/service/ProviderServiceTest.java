package com.think.service;

import com.think.dto.ProviderRegistrationRequest;
import com.think.dto.ProviderResponse;
import com.think.entity.ClinicAddress;
import com.think.entity.Provider;
import com.think.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {
    
    @Mock
    private ProviderRepository providerRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private ProviderService providerService;
    
    private ProviderRegistrationRequest validRequest;
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
        
        ClinicAddress clinicAddress = new ClinicAddress();
        clinicAddress.setStreet("123 Medical Center Dr");
        clinicAddress.setCity("New York");
        clinicAddress.setState("NY");
        clinicAddress.setZip("10001");
        savedProvider.setClinicAddress(clinicAddress);
    }
    
    @Test
    void registerProvider_Success() {
        // Given
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);
        
        // When
        ProviderResponse result = providerService.registerProvider(validRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(savedProvider.getId(), result.getId());
        assertEquals(savedProvider.getFirstName(), result.getFirstName());
        assertEquals(savedProvider.getLastName(), result.getLastName());
        assertEquals(savedProvider.getEmail(), result.getEmail());
        assertEquals(savedProvider.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(savedProvider.getSpecialization(), result.getSpecialization());
        assertEquals(savedProvider.getLicenseNumber(), result.getLicenseNumber());
        assertEquals(savedProvider.getYearsOfExperience(), result.getYearsOfExperience());
        
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(providerRepository).existsByLicenseNumber(validRequest.getLicenseNumber());
        verify(passwordEncoder).encode(validRequest.getPassword());
        verify(providerRepository).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_DuplicateEmail_ThrowsException() {
        // Given
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.registerProvider(validRequest);
        });
        
        assertEquals("Email already registered: " + validRequest.getEmail(), exception.getMessage());
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_DuplicatePhoneNumber_ThrowsException() {
        // Given
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.registerProvider(validRequest);
        });
        
        assertEquals("Phone number already registered: " + validRequest.getPhoneNumber(), exception.getMessage());
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_DuplicateLicenseNumber_ThrowsException() {
        // Given
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.registerProvider(validRequest);
        });
        
        assertEquals("License number already registered: " + validRequest.getLicenseNumber(), exception.getMessage());
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(providerRepository).existsByLicenseNumber(validRequest.getLicenseNumber());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void getProviderById_Success() {
        // Given
        UUID providerId = UUID.randomUUID();
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(savedProvider));
        
        // When
        Optional<ProviderResponse> result = providerService.getProviderById(providerId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(savedProvider.getId(), result.get().getId());
        assertEquals(savedProvider.getFirstName(), result.get().getFirstName());
        verify(providerRepository).findById(providerId);
    }
    
    @Test
    void getProviderById_NotFound() {
        // Given
        UUID providerId = UUID.randomUUID();
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());
        
        // When
        Optional<ProviderResponse> result = providerService.getProviderById(providerId);
        
        // Then
        assertFalse(result.isPresent());
        verify(providerRepository).findById(providerId);
    }
    
    @Test
    void getProviderByEmail_Success() {
        // Given
        String email = "john.doe@example.com";
        when(providerRepository.findByEmail(email)).thenReturn(Optional.of(savedProvider));
        
        // When
        Optional<ProviderResponse> result = providerService.getProviderByEmail(email);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(savedProvider.getEmail(), result.get().getEmail());
        verify(providerRepository).findByEmail(email);
    }
    
    @Test
    void getProviderByEmail_NotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(providerRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When
        Optional<ProviderResponse> result = providerService.getProviderByEmail(email);
        
        // Then
        assertFalse(result.isPresent());
        verify(providerRepository).findByEmail(email);
    }
    
    @Test
    void verifyPassword_Success() {
        // Given
        String rawPassword = "SecurePass123!";
        String encodedPassword = "hashedPassword";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        
        // When
        boolean result = providerService.verifyPassword(rawPassword, encodedPassword);
        
        // Then
        assertTrue(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
    
    @Test
    void verifyPassword_Failure() {
        // Given
        String rawPassword = "WrongPassword123!";
        String encodedPassword = "hashedPassword";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);
        
        // When
        boolean result = providerService.verifyPassword(rawPassword, encodedPassword);
        
        // Then
        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
    
    @Test
    void registerProvider_SanitizesInput() {
        // Given
        validRequest.setFirstName("<script>alert('xss')</script>John");
        validRequest.setLastName("Doe<script>alert('xss')</script>");
        validRequest.setSpecialization("Cardiology<script>alert('xss')</script>");
        
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);
        
        // When
        providerService.registerProvider(validRequest);
        
        // Then
        verify(providerRepository).save(argThat(provider -> 
            provider.getFirstName().equals("John") &&
            provider.getLastName().equals("Doe") &&
            provider.getSpecialization().equals("Cardiology")
        ));
    }
}
