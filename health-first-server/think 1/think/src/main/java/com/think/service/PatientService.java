package com.think.service;

import com.think.dto.PatientLoginRequest;
import com.think.dto.PatientLoginResponse;
import com.think.dto.PatientRegistrationRequest;
import com.think.dto.PatientResponse;
import com.think.entity.*;
import com.think.repository.PatientRepository;
import com.think.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    
    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public PatientResponse registerPatient(PatientRegistrationRequest request) {
        log.info("Registering new patient with email: {}", request.getEmail());
        
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation password do not match");
        }
        
        // Validate age (must be at least 13 years old for COPPA compliance)
        validateAge(request.getDateOfBirth());
        
        // Check for duplicate email
        if (patientRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        
        // Check for duplicate phone number
        if (patientRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
        
        // Hash password with bcrypt (12 salt rounds for security)
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create patient entity
        Patient patient = Patient.builder()
            .firstName(request.getFirstName().trim())
            .lastName(request.getLastName().trim())
            .email(request.getEmail().toLowerCase().trim())
            .phoneNumber(request.getPhoneNumber().trim())
            .passwordHash(hashedPassword)
            .dateOfBirth(request.getDateOfBirth())
            .gender(request.getGender())
            .address(convertToPatientAddress(request.getAddress()))
            .emergencyContact(convertToEmergencyContact(request.getEmergencyContact()))
            .medicalHistory(request.getMedicalHistory())
            .insuranceInfo(convertToInsuranceInfo(request.getInsuranceInfo()))
            .emailVerified(false)
            .phoneVerified(false)
            .isActive(true)
            .build();
        
        // Save patient
        Patient savedPatient = patientRepository.save(patient);
        
        log.info("Patient registered successfully with ID: {}", savedPatient.getId());
        
        return PatientResponse.fromPatient(savedPatient);
    }
    
    @Transactional(readOnly = true)
    public PatientLoginResponse loginPatient(PatientLoginRequest request) {
        log.info("Patient login attempt for email: {}", request.getEmail());
        
        // Find patient by email
        Patient patient = patientRepository.findByEmail(request.getEmail().toLowerCase().trim())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Check if patient is active
        if (!patient.getIsActive()) {
            throw new IllegalArgumentException("Account is deactivated. Please contact support.");
        }
        
        // Verify password using bcrypt
        if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Generate JWT token with 30-minute expiry (1800 seconds)
        Map<String, Object> claims = new HashMap<>();
        claims.put("patient_id", patient.getId());
        claims.put("email", patient.getEmail());
        claims.put("role", "PATIENT");
        
        String accessToken = jwtUtil.generateToken(claims, patient.getEmail(), 1800L);
        
        // Create patient data for response
        PatientLoginResponse.PatientData patientData = PatientLoginResponse.PatientData.builder()
            .patientId(patient.getId())
            .email(patient.getEmail())
            .firstName(patient.getFirstName())
            .lastName(patient.getLastName())
            .phoneNumber(patient.getPhoneNumber())
            .emailVerified(patient.getEmailVerified())
            .phoneVerified(patient.getPhoneVerified())
            .isActive(patient.getIsActive())
            .build();
        
        // Create login response
        PatientLoginResponse response = PatientLoginResponse.builder()
            .accessToken(accessToken)
            .expiresIn(1800L) // 30 minutes in seconds
            .tokenType("Bearer")
            .patient(patientData)
            .build();
        
        log.info("Patient login successful for email: {}", request.getEmail());
        
        return response;
    }
    
    private void validateAge(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(dateOfBirth, today);
        
        if (age.getYears() < 13) {
            throw new IllegalArgumentException("Patient must be at least 13 years old for COPPA compliance");
        }
    }
    
    private PatientAddress convertToPatientAddress(PatientRegistrationRequest.PatientAddressRequest addressRequest) {
        if (addressRequest == null) {
            return null;
        }
        
        return new PatientAddress(
            addressRequest.getStreet().trim(),
            addressRequest.getCity().trim(),
            addressRequest.getState().trim(),
            addressRequest.getZip().trim()
        );
    }
    
    private EmergencyContact convertToEmergencyContact(PatientRegistrationRequest.EmergencyContactRequest contactRequest) {
        if (contactRequest == null) {
            return null;
        }
        
        return new EmergencyContact(
            contactRequest.getName() != null ? contactRequest.getName().trim() : null,
            contactRequest.getPhone() != null ? contactRequest.getPhone().trim() : null,
            contactRequest.getRelationship() != null ? contactRequest.getRelationship().trim() : null
        );
    }
    
    private InsuranceInfo convertToInsuranceInfo(PatientRegistrationRequest.InsuranceInfoRequest insuranceRequest) {
        if (insuranceRequest == null) {
            return null;
        }
        
        return new InsuranceInfo(
            insuranceRequest.getProvider() != null ? insuranceRequest.getProvider().trim() : null,
            insuranceRequest.getPolicyNumber() != null ? insuranceRequest.getPolicyNumber().trim() : null
        );
    }
}
