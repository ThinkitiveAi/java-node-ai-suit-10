package com.think.service;

import com.think.dto.ProviderLoginRequest;
import com.think.dto.ProviderLoginResponse;
import com.think.dto.ProviderRegistrationRequest;
import com.think.dto.ProviderResponse;
import com.think.entity.ClinicAddress;
import com.think.entity.Provider;
import com.think.repository.ProviderRepository;
import com.think.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProviderService {
    
    private final ProviderRepository providerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public ProviderResponse registerProvider(ProviderRegistrationRequest request) {
        log.info("Registering new provider with email: {}", request.getEmail());
        
        // Check for duplicate email
        if (providerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        
        // Check for duplicate phone number
        if (providerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered: " + request.getPhoneNumber());
        }
        
        // Check for duplicate license number
        if (providerRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already registered: " + request.getLicenseNumber());
        }
        
        // Create provider entity
        Provider provider = new Provider();
        provider.setFirstName(sanitizeInput(request.getFirstName()));
        provider.setLastName(sanitizeInput(request.getLastName()));
        provider.setEmail(request.getEmail().toLowerCase().trim());
        provider.setPhoneNumber(request.getPhoneNumber().trim());
        provider.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        provider.setSpecialization(sanitizeInput(request.getSpecialization()));
        provider.setLicenseNumber(request.getLicenseNumber().toUpperCase().trim());
        provider.setYearsOfExperience(request.getYearsOfExperience());
        
        // Set clinic address
        ClinicAddress clinicAddress = new ClinicAddress();
        clinicAddress.setStreet(sanitizeInput(request.getClinicAddress().getStreet()));
        clinicAddress.setCity(sanitizeInput(request.getClinicAddress().getCity()));
        clinicAddress.setState(sanitizeInput(request.getClinicAddress().getState()));
        clinicAddress.setZip(request.getClinicAddress().getZip().trim());
        provider.setClinicAddress(clinicAddress);
        
        // Save provider
        Provider savedProvider = providerRepository.save(provider);
        log.info("Provider registered successfully with ID: {}", savedProvider.getId());
        
        return ProviderResponse.fromProvider(savedProvider);
    }
    
    public List<ProviderResponse> getAllProviders(boolean activeOnly, String specialization) {
        log.info("Fetching all providers with activeOnly={}, specialization={}", activeOnly, specialization);
        
        List<Provider> providers;
        
        if (activeOnly && specialization != null && !specialization.trim().isEmpty()) {
            providers = providerRepository.findByIsActiveTrueAndSpecializationContainingIgnoreCase(specialization.trim());
        } else if (activeOnly) {
            providers = providerRepository.findByIsActiveTrue();
        } else if (specialization != null && !specialization.trim().isEmpty()) {
            providers = providerRepository.findBySpecializationContainingIgnoreCase(specialization.trim());
        } else {
            providers = providerRepository.findAll();
        }
        
        return providers.stream()
                .map(ProviderResponse::fromProvider)
                .collect(Collectors.toList());
    }
    
    public Optional<ProviderResponse> getProviderById(UUID id) {
        return providerRepository.findById(id)
                .map(ProviderResponse::fromProvider);
    }
    
    public Optional<ProviderResponse> getProviderByEmail(String email) {
        return providerRepository.findByEmail(email)
                .map(ProviderResponse::fromProvider);
    }
    
    public ProviderLoginResponse loginProvider(ProviderLoginRequest request) {
        log.info("Attempting login for provider with email: {}", request.getEmail());
        
        // Find provider by email
        Optional<Provider> providerOpt = providerRepository.findByEmail(request.getEmail().toLowerCase().trim());
        
        if (providerOpt.isEmpty()) {
            log.warn("Login failed: Provider not found with email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        Provider provider = providerOpt.get();
        
        // Check if provider is active
        if (!provider.getIsActive()) {
            log.warn("Login failed: Inactive provider with email: {}", request.getEmail());
            throw new IllegalArgumentException("Account is deactivated. Please contact support.");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), provider.getPasswordHash())) {
            log.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(provider.getId(), provider.getEmail(), provider.getSpecialization());
        
        log.info("Login successful for provider with email: {}", request.getEmail());
        
        return ProviderLoginResponse.success(token, jwtUtil.getExpirationTime(), ProviderResponse.fromProvider(provider));
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Sanitize input to prevent injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potential script tags and dangerous characters
        return input.trim()
                .replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]*>", "")
                .replaceAll("javascript:", "")
                .replaceAll("on\\w+\\s*=", "");
    }
}
