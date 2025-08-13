package com.think.dto;

import com.think.entity.Provider;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProviderResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private ClinicAddressResponse clinicAddress;
    private Provider.VerificationStatus verificationStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProviderResponse fromProvider(Provider provider) {
        ProviderResponse response = new ProviderResponse();
        response.setId(provider.getId());
        response.setFirstName(provider.getFirstName());
        response.setLastName(provider.getLastName());
        response.setEmail(provider.getEmail());
        response.setPhoneNumber(provider.getPhoneNumber());
        response.setSpecialization(provider.getSpecialization());
        response.setLicenseNumber(provider.getLicenseNumber());
        response.setYearsOfExperience(provider.getYearsOfExperience());
        response.setVerificationStatus(provider.getVerificationStatus());
        response.setIsActive(provider.getIsActive());
        response.setCreatedAt(provider.getCreatedAt());
        response.setUpdatedAt(provider.getUpdatedAt());
        
        if (provider.getClinicAddress() != null) {
            ClinicAddressResponse addressResponse = new ClinicAddressResponse();
            addressResponse.setStreet(provider.getClinicAddress().getStreet());
            addressResponse.setCity(provider.getClinicAddress().getCity());
            addressResponse.setState(provider.getClinicAddress().getState());
            addressResponse.setZip(provider.getClinicAddress().getZip());
            response.setClinicAddress(addressResponse);
        }
        
        return response;
    }
    
    @Data
    public static class ClinicAddressResponse {
        private String street;
        private String city;
        private String state;
        private String zip;
    }
}
