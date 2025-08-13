package com.think.controller;

import com.think.dto.*;
import com.think.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Appointment Management", description = "APIs for booking and managing healthcare appointments")
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping("/book")
    @Operation(
        summary = "Book Appointment",
        description = "Schedule a new appointment by selecting patient, provider, appointment type, date & time, and reason for visit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Appointment booked successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "appointmentId": "uuid-here",
                      "bookingReference": "APT-12345678",
                      "patientId": "patient-uuid",
                      "patientName": "John Doe",
                      "providerId": "provider-uuid",
                      "providerName": "Dr. Jane Smith",
                      "appointmentDate": "2024-02-15",
                      "appointmentTime": "10:00:00",
                      "appointmentType": "CONSULTATION",
                      "appointmentMode": "IN_PERSON",
                      "reasonForVisit": "Regular checkup and consultation",
                      "status": "BOOKED",
                      "estimatedCost": 150.00,
                      "currency": "USD"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request data or booking conflicts"),
        @ApiResponse(responseCode = "404", description = "Patient or provider not found")
    })
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        try {
            log.info("Received appointment booking request for patient: {}, provider: {}", 
                    request.getPatientId(), request.getProviderId());
            
            AppointmentResponse response = appointmentService.bookAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Appointment booking failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during appointment booking", e);
            throw new RuntimeException("Appointment booking failed due to an internal error", e);
        }
    }
    
    @GetMapping
    @Operation(
        summary = "Get Appointment List",
        description = "Retrieve a paginated list of appointments with filters for date range, appointment type, provider, and patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Appointments retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentListResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<AppointmentListResponse> getAppointments(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-02-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-02-28")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Appointment type", example = "CONSULTATION")
            @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Provider ID", example = "provider-uuid")
            @RequestParam(required = false) String providerId,
            @Parameter(description = "Patient ID", example = "patient-uuid")
            @RequestParam(required = false) String patientId,
            @Parameter(description = "Appointment status", example = "BOOKED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field", example = "appointmentDateTime")
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        try {
            log.info("Retrieving appointments with filters: startDate={}, endDate={}, appointmentType={}, providerId={}, patientId={}, status={}", 
                    startDate, endDate, appointmentType, providerId, patientId, status);
            
            AppointmentListRequest request = new AppointmentListRequest();
            request.setStartDate(startDate != null ? java.time.LocalDate.parse(startDate) : null);
            request.setEndDate(endDate != null ? java.time.LocalDate.parse(endDate) : null);
            request.setAppointmentType(appointmentType);
            request.setProviderId(providerId != null ? java.util.UUID.fromString(providerId) : null);
            request.setPatientId(patientId != null ? java.util.UUID.fromString(patientId) : null);
            request.setStatus(status);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);
            
            AppointmentListResponse response = appointmentService.getAppointments(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving appointments", e);
            throw new RuntimeException("Failed to retrieve appointments", e);
        }
    }
    
    @GetMapping("/{bookingReference}")
    @Operation(
        summary = "Get Appointment by Booking Reference",
        description = "Retrieve appointment details by booking reference"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Appointment retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ResponseEntity<AppointmentResponse> getAppointmentByBookingReference(
            @Parameter(description = "Booking reference", example = "APT-12345678")
            @PathVariable String bookingReference) {
        
        try {
            log.info("Retrieving appointment with booking reference: {}", bookingReference);
            
            AppointmentResponse response = appointmentService.getAppointmentByBookingReference(bookingReference);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Appointment not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving appointment", e);
            throw new RuntimeException("Failed to retrieve appointment", e);
        }
    }
    
    @PutMapping("/{bookingReference}/cancel")
    @Operation(
        summary = "Cancel Appointment",
        description = "Cancel an existing appointment by booking reference"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Appointment cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Appointment cannot be cancelled"),
        @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @Parameter(description = "Booking reference", example = "APT-12345678")
            @PathVariable String bookingReference) {
        
        try {
            log.info("Cancelling appointment with booking reference: {}", bookingReference);
            
            AppointmentResponse response = appointmentService.cancelAppointment(bookingReference);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Appointment cancellation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error cancelling appointment", e);
            throw new RuntimeException("Failed to cancel appointment", e);
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
