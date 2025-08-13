package com.think.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "provider_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderAvailability {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @NotNull(message = "Provider is required")
    private Provider provider;
    
    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;
    
    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @NotBlank(message = "Timezone is required")
    @Column(nullable = false)
    private String timezone;
    
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    private RecurrencePattern recurrencePattern;
    
    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;
    
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration = 30;
    
    @Min(value = 0, message = "Break duration cannot be negative")
    @Max(value = 120, message = "Break duration cannot exceed 2 hours")
    @Column(name = "break_duration", nullable = false)
    private Integer breakDuration = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
    
    @Min(value = 1, message = "Maximum appointments per slot must be at least 1")
    @Max(value = 10, message = "Maximum appointments per slot cannot exceed 10")
    @Column(name = "max_appointments_per_slot", nullable = false)
    private Integer maxAppointmentsPerSlot = 1;
    
    @Min(value = 0, message = "Current appointments cannot be negative")
    @Column(name = "current_appointments", nullable = false)
    private Integer currentAppointments = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;
    
    @Embedded
    @Valid
    @NotNull(message = "Location is required")
    private AvailabilityLocation location;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "baseFee", column = @Column(name = "base_fee")),
        @AttributeOverride(name = "insuranceAccepted", column = @Column(name = "insurance_accepted")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private AvailabilityPricing pricing;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ElementCollection
    @CollectionTable(name = "availability_special_requirements", 
                     joinColumns = @JoinColumn(name = "availability_id"))
    @Column(name = "requirement")
    private List<String> specialRequirements;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum RecurrencePattern {
        DAILY, WEEKLY, MONTHLY
    }
    
    public enum AvailabilityStatus {
        AVAILABLE, BOOKED, CANCELLED, BLOCKED, MAINTENANCE
    }
    
    public enum AppointmentType {
        CONSULTATION, FOLLOW_UP, EMERGENCY, TELEMEDICINE
    }
    
    public enum LocationType {
        CLINIC, HOSPITAL, TELEMEDICINE, HOME_VISIT
    }
}
