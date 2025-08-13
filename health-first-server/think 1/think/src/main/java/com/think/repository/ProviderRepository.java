package com.think.repository;

import com.think.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    
    Optional<Provider> findByEmail(String email);
    
    Optional<Provider> findByPhoneNumber(String phoneNumber);
    
    Optional<Provider> findByLicenseNumber(String licenseNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByLicenseNumber(String licenseNumber);
    
    List<Provider> findByIsActiveTrue();
    
    List<Provider> findBySpecializationContainingIgnoreCase(String specialization);
    
    List<Provider> findByIsActiveTrueAndSpecializationContainingIgnoreCase(String specialization);
    
    @Query("SELECT p FROM Provider p WHERE p.email = :email AND p.id != :id")
    Optional<Provider> findByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);
    
    @Query("SELECT p FROM Provider p WHERE p.phoneNumber = :phoneNumber AND p.id != :id")
    Optional<Provider> findByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") UUID id);
    
    @Query("SELECT p FROM Provider p WHERE p.licenseNumber = :licenseNumber AND p.id != :id")
    Optional<Provider> findByLicenseNumberAndIdNot(@Param("licenseNumber") String licenseNumber, @Param("id") UUID id);
}
