package com.think.repository;

import com.think.entity.ProviderAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, String> {
    
    List<ProviderAvailability> findByProvider_IdAndDateBetweenOrderByDateAscStartTimeAsc(
        UUID providerId, LocalDate startDate, LocalDate endDate);
    
    List<ProviderAvailability> findByProvider_IdAndDateBetweenAndStatusOrderByDateAscStartTimeAsc(
        UUID providerId, LocalDate startDate, LocalDate endDate, ProviderAvailability.AvailabilityStatus status);
    
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND (:status IS NULL OR pa.status = :status) " +
           "AND (:appointmentType IS NULL OR pa.appointmentType = :appointmentType) " +
           "ORDER BY pa.date ASC, pa.startTime ASC")
    List<ProviderAvailability> findAvailabilityWithFilters(
        @Param("providerId") UUID providerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") ProviderAvailability.AvailabilityStatus status,
        @Param("appointmentType") ProviderAvailability.AppointmentType appointmentType);
    
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.specialization = :specialization " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.status = 'AVAILABLE' " +
           "AND (:appointmentType IS NULL OR pa.appointmentType = :appointmentType) " +
           "AND (:insuranceAccepted IS NULL OR pa.pricing.insuranceAccepted = :insuranceAccepted) " +
           "AND (:maxPrice IS NULL OR pa.pricing.baseFee <= :maxPrice) " +
           "ORDER BY pa.date ASC, pa.startTime ASC")
    Page<ProviderAvailability> searchAvailableSlots(
        @Param("specialization") String specialization,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("appointmentType") ProviderAvailability.AppointmentType appointmentType,
        @Param("insuranceAccepted") Boolean insuranceAccepted,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable);
    
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.date = :date " +
           "AND ((pa.startTime <= :startTime AND pa.endTime > :startTime) " +
           "OR (pa.startTime < :endTime AND pa.endTime >= :endTime) " +
           "OR (pa.startTime >= :startTime AND pa.endTime <= :endTime))")
    List<ProviderAvailability> findOverlappingSlots(
        @Param("providerId") UUID providerId,
        @Param("date") LocalDate date,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);
    
    List<ProviderAvailability> findByProvider_IdAndIsRecurringTrue(UUID providerId);
    
    Optional<ProviderAvailability> findByIdAndProvider_Id(String id, UUID providerId);
    
    @Query("SELECT COUNT(pa) FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.date BETWEEN :startDate AND :endDate")
    long countByProviderAndDateRange(
        @Param("providerId") UUID providerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
