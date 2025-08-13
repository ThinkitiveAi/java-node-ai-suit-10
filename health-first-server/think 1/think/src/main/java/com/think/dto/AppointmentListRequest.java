package com.think.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentListRequest {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String appointmentType;
    private UUID providerId;
    private UUID patientId;
    private String status;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "appointmentDateTime";
    private String sortDirection = "DESC";
}
