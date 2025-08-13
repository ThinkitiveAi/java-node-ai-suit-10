package com.think.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityLocation {
    
    @NotNull(message = "Location type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private ProviderAvailability.LocationType type;
    
    @NotBlank(message = "Address is required for physical locations")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    @Column(name = "address")
    private String address;
    
    @Size(max = 50, message = "Room number cannot exceed 50 characters")
    @Column(name = "room_number")
    private String roomNumber;
}
