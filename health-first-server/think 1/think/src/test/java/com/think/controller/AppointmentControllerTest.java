package com.think.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.think.dto.AppointmentListResponse;
import com.think.dto.AppointmentResponse;
import com.think.dto.BookAppointmentRequest;
import com.think.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private BookAppointmentRequest testRequest;
    private AppointmentResponse testResponse;
    private AppointmentListResponse testListResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
        objectMapper = new ObjectMapper();

        // Setup test request
        testRequest = new BookAppointmentRequest();
        testRequest.setPatientId(UUID.randomUUID());
        testRequest.setProviderId(UUID.randomUUID());
        testRequest.setAppointmentDate(LocalDate.of(2024, 2, 15));
        testRequest.setAppointmentTime(LocalTime.of(10, 0));
        testRequest.setAppointmentType("CONSULTATION");
        testRequest.setAppointmentMode("IN_PERSON");
        testRequest.setReasonForVisit("Regular checkup and consultation");
        testRequest.setAdditionalNotes("Patient prefers morning appointments");
        testRequest.setInsuranceProvider("Blue Cross");
        testRequest.setInsurancePolicyNumber("BC123456");

        // Setup test response
        testResponse = new AppointmentResponse();
        testResponse.setAppointmentId("slot-123");
        testResponse.setBookingReference("APT-12345678");
        testResponse.setPatientId(testRequest.getPatientId());
        testResponse.setPatientName("John Doe");
        testResponse.setProviderId(testRequest.getProviderId());
        testResponse.setProviderName("Dr. Jane Smith");
        testResponse.setAppointmentDate(testRequest.getAppointmentDate());
        testResponse.setAppointmentTime(testRequest.getAppointmentTime());
        testResponse.setAppointmentType(testRequest.getAppointmentType());
        testResponse.setAppointmentMode(testRequest.getAppointmentMode());
        testResponse.setReasonForVisit(testRequest.getReasonForVisit());
        testResponse.setStatus("BOOKED");
        testResponse.setEstimatedCost(BigDecimal.valueOf(150.00));
        testResponse.setCurrency("USD");

        // Setup test list response
        testListResponse = new AppointmentListResponse();
        testListResponse.setAppointments(List.of(testResponse));
        testListResponse.setCurrentPage(0);
        testListResponse.setTotalPages(1);
        testListResponse.setTotalElements(1);
        testListResponse.setPageSize(20);
        testListResponse.setHasNext(false);
        testListResponse.setHasPrevious(false);
    }

    @Test
    void bookAppointment_Success() throws Exception {
        // Arrange
        when(appointmentService.bookAppointment(any(BookAppointmentRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value("slot-123"))
                .andExpect(jsonPath("$.bookingReference").value("APT-12345678"))
                .andExpect(jsonPath("$.patientName").value("John Doe"))
                .andExpect(jsonPath("$.providerName").value("Dr. Jane Smith"))
                .andExpect(jsonPath("$.appointmentType").value("CONSULTATION"))
                .andExpect(jsonPath("$.appointmentMode").value("IN_PERSON"))
                .andExpect(jsonPath("$.reasonForVisit").value("Regular checkup and consultation"))
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andExpect(jsonPath("$.estimatedCost").value(150.00))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(appointmentService).bookAppointment(any(BookAppointmentRequest.class));
    }

    @Test
    void bookAppointment_ValidationError() throws Exception {
        // Arrange
        testRequest.setReasonForVisit(""); // Invalid - too short

        // Act & Assert
        mockMvc.perform(post("/api/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reasonForVisit").exists());

        verify(appointmentService, never()).bookAppointment(any());
    }

    @Test
    void bookAppointment_ServiceException() throws Exception {
        // Arrange
        when(appointmentService.bookAppointment(any(BookAppointmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Patient not found"));

        // Act & Assert
        mockMvc.perform(post("/api/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Patient not found"));

        verify(appointmentService).bookAppointment(any(BookAppointmentRequest.class));
    }

    @Test
    void getAppointments_Success() throws Exception {
        // Arrange
        when(appointmentService.getAppointments(any())).thenReturn(testListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/appointments")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "appointmentDateTime")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray())
                .andExpect(jsonPath("$.appointments[0].appointmentId").value("slot-123"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        verify(appointmentService).getAppointments(any());
    }

    @Test
    void getAppointments_WithFilters() throws Exception {
        // Arrange
        when(appointmentService.getAppointments(any())).thenReturn(testListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/appointments")
                .param("startDate", "2024-02-01")
                .param("endDate", "2024-02-28")
                .param("appointmentType", "CONSULTATION")
                .param("providerId", testRequest.getProviderId().toString())
                .param("patientId", testRequest.getPatientId().toString())
                .param("status", "BOOKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());

        verify(appointmentService).getAppointments(any());
    }

    @Test
    void getAppointmentByBookingReference_Success() throws Exception {
        // Arrange
        when(appointmentService.getAppointmentByBookingReference("APT-12345678")).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/APT-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value("slot-123"))
                .andExpect(jsonPath("$.bookingReference").value("APT-12345678"))
                .andExpect(jsonPath("$.patientName").value("John Doe"))
                .andExpect(jsonPath("$.providerName").value("Dr. Jane Smith"));

        verify(appointmentService).getAppointmentByBookingReference("APT-12345678");
    }

    @Test
    void getAppointmentByBookingReference_NotFound() throws Exception {
        // Arrange
        when(appointmentService.getAppointmentByBookingReference("APT-12345678"))
                .thenThrow(new IllegalArgumentException("Appointment not found with booking reference: APT-12345678"));

        // Act & Assert
        mockMvc.perform(get("/api/appointments/APT-12345678"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Appointment not found with booking reference: APT-12345678"));

        verify(appointmentService).getAppointmentByBookingReference("APT-12345678");
    }

    @Test
    void cancelAppointment_Success() throws Exception {
        // Arrange
        testResponse.setStatus("CANCELLED");
        when(appointmentService.cancelAppointment("APT-12345678")).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(put("/api/appointments/APT-12345678/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value("slot-123"))
                .andExpect(jsonPath("$.bookingReference").value("APT-12345678"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(appointmentService).cancelAppointment("APT-12345678");
    }

    @Test
    void cancelAppointment_NotFound() throws Exception {
        // Arrange
        when(appointmentService.cancelAppointment("APT-12345678"))
                .thenThrow(new IllegalArgumentException("Appointment not found with booking reference: APT-12345678"));

        // Act & Assert
        mockMvc.perform(put("/api/appointments/APT-12345678/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Appointment not found with booking reference: APT-12345678"));

        verify(appointmentService).cancelAppointment("APT-12345678");
    }

    @Test
    void cancelAppointment_CannotCancel() throws Exception {
        // Arrange
        when(appointmentService.cancelAppointment("APT-12345678"))
                .thenThrow(new IllegalArgumentException("Appointment is not in BOOKED status and cannot be cancelled"));

        // Act & Assert
        mockMvc.perform(put("/api/appointments/APT-12345678/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Appointment is not in BOOKED status and cannot be cancelled"));

        verify(appointmentService).cancelAppointment("APT-12345678");
    }
}
