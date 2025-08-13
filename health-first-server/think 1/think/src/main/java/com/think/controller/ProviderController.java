package com.think.controller;

import com.think.dto.ProviderRegistrationRequest;
import com.think.dto.ProviderResponse;
import com.think.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProviderController {
    
    private final ProviderService providerService;
    
    @PostMapping("/register")
    public ResponseEntity<ProviderResponse> registerProvider(@Valid @RequestBody ProviderRegistrationRequest request) {
        try {
            log.info("Received provider registration request for email: {}", request.getEmail());
            ProviderResponse response = providerService.registerProvider(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during provider registration for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Registration failed due to an internal error", e);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ProviderResponse>> getAllProviders(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false) String specialization) {
        try {
            log.info("Fetching all providers with activeOnly={}, specialization={}", activeOnly, specialization);
            List<ProviderResponse> providers = providerService.getAllProviders(activeOnly, specialization);
            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            log.error("Error retrieving providers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve providers", e);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProviderById(@PathVariable UUID id) {
        try {
            Optional<ProviderResponse> provider = providerService.getProviderById(id);
            return provider.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving provider with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve provider", e);
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ProviderResponse> getProviderByEmail(@PathVariable String email) {
        try {
            Optional<ProviderResponse> provider = providerService.getProviderByEmail(email);
            return provider.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving provider with email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve provider", e);
        }
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "An internal server error occurred");
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
