package com.think.controller;

import com.think.dto.PatientLoginRequest;
import com.think.dto.PatientLoginResponse;
import com.think.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PatientAuthController {
    
    private final PatientService patientService;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginPatient(@Valid @RequestBody PatientLoginRequest request) {
        try {
            log.info("Patient login request received for email: {}", request.getEmail());
            
            PatientLoginResponse loginResponse = patientService.loginPatient(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("data", loginResponse);
            
            log.info("Patient login successful for email: {}", request.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Patient login failed for email: {} - {}", request.getEmail(), e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("Unexpected error during patient login for email: {}", request.getEmail(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An unexpected error occurred. Please try again later.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
