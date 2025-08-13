package com.think.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Provider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format (e.g., +1234567890)")
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    
    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @NotBlank(message = "Specialization is required")
    @Size(min = 3, max = 100, message = "Specialization must be between 3 and 100 characters")
    @Column(nullable = false)
    private String specialization;
    
    @NotBlank(message = "License number is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "License number must be alphanumeric")
    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;
    
    @Min(value = 0, message = "Years of experience must be at least 0")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "clinic_street", nullable = false, length = 200)),
        @AttributeOverride(name = "city", column = @Column(name = "clinic_city", nullable = false, length = 100)),
        @AttributeOverride(name = "state", column = @Column(name = "clinic_state", nullable = false, length = 50)),
        @AttributeOverride(name = "zip", column = @Column(name = "clinic_zip", nullable = false))
    })
    private ClinicAddress clinicAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}
