package com.think.controller;

import com.think.dto.*;
import com.think.entity.ProviderAvailability;
import com.think.service.ProviderAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Provider Availability Management", description = "APIs for managing healthcare provider availability and appointment slots")
public class ProviderAvailabilityController {
    
    private final ProviderAvailabilityService availabilityService;
    
    @PostMapping("/availability")
    @Operation(
        summary = "Create Availability Slots",
        description = "Create new availability slots for a healthcare provider with optional recurring patterns",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Availability creation request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateAvailabilityRequest.class),
                examples = @ExampleObject(
                    name = "Standard Consultation",
                    value = """
                    {
                      "date": "2024-02-15",
                      "start_time": "09:00",
                      "end_time": "17:00",
                      "timezone": "America/New_York",
                      "slot_duration": 30,
                      "break_duration": 15,
                      "is_recurring": true,
                      "recurrence_pattern": "WEEKLY",
                      "recurrence_end_date": "2024-08-15",
                      "appointment_type": "CONSULTATION",
                      "location": {
                        "type": "CLINIC",
                        "address": "123 Medical Center Dr, New York, NY 10001",
                        "room_number": "Room 205"
                      },
                      "pricing": {
                        "base_fee": 150.00,
                        "insurance_accepted": true,
                        "currency": "USD"
                      },
                      "special_requirements": ["fasting_required", "bring_insurance_card"],
                      "notes": "Standard consultation slots"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Availability slots created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AvailabilityResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "Availability slots created successfully",
                      "data": {
                        "availability_id": "uuid-here",
                        "slots_created": 32,
                        "date_range": {
                          "start": "2024-02-15",
                          "end": "2024-08-15"
                        },
                        "total_appointments_available": 224
                      }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request data or overlapping slots"),
        @ApiResponse(responseCode = "404", description = "Provider not found"),
        @ApiResponse(responseCode = "422", description = "Validation errors")
    })
    public ResponseEntity<Map<String, Object>> createAvailability(
            @Parameter(description = "Provider ID", required = true, example = "provider-uuid-123")
            @RequestParam String providerId,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        
        try {
            log.info("Creating availability for provider: {}", providerId);
            
            AvailabilityResponse response = availabilityService.createAvailability(providerId, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Availability slots created successfully");
            result.put("data", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create availability for provider {}: {}", providerId, e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("Unexpected error creating availability for provider: {}", providerId, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    @GetMapping("/{providerId}/availability")
    @Operation(
        summary = "Get Provider Availability",
        description = "Retrieve availability slots for a specific provider within a date range"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Provider availability retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AvailabilityResponse.ProviderAvailabilityResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<Map<String, Object>> getProviderAvailability(
            @Parameter(description = "Provider ID", required = true, example = "provider-uuid-123")
            @PathVariable String providerId,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2024-02-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2024-02-20")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by status", example = "AVAILABLE")
            @RequestParam(required = false) ProviderAvailability.AvailabilityStatus status,
            @Parameter(description = "Filter by appointment type", example = "CONSULTATION")
            @RequestParam(required = false) ProviderAvailability.AppointmentType appointmentType) {
        
        try {
            log.info("Getting availability for provider: {} from {} to {}", providerId, startDate, endDate);
            
            AvailabilityResponse.ProviderAvailabilityResponse response = 
                availabilityService.getProviderAvailability(providerId, startDate, endDate, status, appointmentType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get availability for provider {}: {}", providerId, e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("Unexpected error getting availability for provider: {}", providerId, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "An unexpected error occurred. Please try again later.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    @PutMapping("/availability/{slotId}")
    @Operation(
        summary = "Update Availability Slot",
        description = "Update specific availability slot details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slot updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "404", description = "Slot not found"),
        @ApiResponse(responseCode = "403", description = "Slot does not belong to provider")
    })
    public ResponseEntity<Map<String, Object>> updateAvailabilitySlot(
            @Parameter(description = "Slot ID", required = true, example = "slot-uuid-123")
            @PathVariable String slotId,
            @Parameter(description = "Provider ID", required = true, example = "provider-uuid-123")
            @RequestParam String providerId,
            @RequestBody Map<String, Object> updates) {
        
        try {
            log.info("Updating availability slot: {} for provider: {}", slotId, providerId);
            
            availabilityService.updateAvailabilitySlot(slotId, providerId, updates);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Availability slot updated successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update slot {} for provider {}: {}", slotId, providerId, e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("Unexpected error updating slot {} for provider: {}", slotId, providerId, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "An unexpected error occurred. Please try again later.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    @DeleteMapping("/availability/{slotId}")
    @Operation(
        summary = "Delete Availability Slot",
        description = "Delete a specific availability slot with optional recurring deletion"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slot deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete booked slot"),
        @ApiResponse(responseCode = "404", description = "Slot not found"),
        @ApiResponse(responseCode = "403", description = "Slot does not belong to provider")
    })
    public ResponseEntity<Map<String, Object>> deleteAvailabilitySlot(
            @Parameter(description = "Slot ID", required = true, example = "slot-uuid-123")
            @PathVariable String slotId,
            @Parameter(description = "Provider ID", required = true, example = "provider-uuid-123")
            @RequestParam String providerId,
            @Parameter(description = "Delete all recurring instances", example = "false")
            @RequestParam(defaultValue = "false") boolean deleteRecurring,
            @Parameter(description = "Reason for deletion", example = "Schedule change")
            @RequestParam(required = false) String reason) {
        
        try {
            log.info("Deleting availability slot: {} for provider: {}", slotId, providerId);
            
            availabilityService.deleteAvailabilitySlot(slotId, providerId, deleteRecurring, reason);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Availability slot deleted successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete slot {} for provider {}: {}", slotId, providerId, e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("Unexpected error deleting slot {} for provider: {}", slotId, providerId, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "An unexpected error occurred. Please try again later.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Test endpoint working - updated code");
        result.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/availability/search")
    @Operation(
        summary = "Search Available Slots",
        description = "Search for available appointment slots based on various criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AvailabilitySearchResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    public ResponseEntity<Map<String, Object>> searchAvailableSlots(
            @Parameter(description = "Specific date to search", example = "2024-02-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start date for range search", example = "2024-02-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for range search", example = "2024-02-20")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Provider specialization", example = "Cardiology")
            @RequestParam(required = false) String specialization,
            @Parameter(description = "Location (city, state, or zip)", example = "New York, NY")
            @RequestParam(required = false) String location,
            @Parameter(description = "Appointment type", example = "CONSULTATION")
            @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Insurance accepted", example = "true")
            @RequestParam(required = false) Boolean insuranceAccepted,
            @Parameter(description = "Maximum price", example = "200.00")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Timezone", example = "America/New_York")
            @RequestParam(required = false) String timezone,
            @Parameter(description = "Available slots only", example = "true")
            @RequestParam(defaultValue = "true") boolean availableOnly) {
        
        try {
            log.info("Searching available slots with criteria: specialization={}, location={}, date={}", 
                    specialization, location, date);
            
            AvailabilitySearchRequest request = new AvailabilitySearchRequest();
            request.setDate(date);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setSpecialization(specialization);
            request.setLocation(location);
            request.setAppointmentType(appointmentType);
            request.setInsuranceAccepted(insuranceAccepted);
            request.setMaxPrice(maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null);
            request.setTimezone(timezone);
            request.setAvailableOnly(availableOnly);
            
            AvailabilitySearchResponse response = availabilityService.searchAvailableSlots(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Unexpected error searching available slots", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "An unexpected error occurred. Please try again later.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Validation failed");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        result.put("errors", errors);
        
        return ResponseEntity.badRequest().body(result);
    }
}
