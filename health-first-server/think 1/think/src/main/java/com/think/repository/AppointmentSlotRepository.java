package com.think.repository;

import com.think.entity.AppointmentSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, String> {
    
    @Query("SELECT a FROM AppointmentSlot a WHERE a.provider.id = :providerId AND a.slotStartTime >= :startTime AND a.slotStartTime < :endTime AND a.status = 'AVAILABLE'")
    List<AppointmentSlot> findAvailableSlotsByProviderAndTimeRange(
            @Param("providerId") UUID providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a FROM AppointmentSlot a WHERE a.provider.id = :providerId AND a.slotStartTime = :appointmentTime AND a.status = 'AVAILABLE'")
    Optional<AppointmentSlot> findAvailableSlotByProviderAndTime(
            @Param("providerId") UUID providerId,
            @Param("appointmentTime") LocalDateTime appointmentTime);
    
    @Query("SELECT a FROM AppointmentSlot a WHERE a.patient.id = :patientId ORDER BY a.slotStartTime DESC")
    Page<AppointmentSlot> findByPatientId(@Param("patientId") String patientId, Pageable pageable);
    
    @Query("SELECT a FROM AppointmentSlot a WHERE a.provider.id = :providerId ORDER BY a.slotStartTime DESC")
    Page<AppointmentSlot> findByProviderId(@Param("providerId") UUID providerId, Pageable pageable);
    
    @Query("SELECT a FROM AppointmentSlot a WHERE " +
           "(:startDate IS NULL OR a.slotStartTime >= :startDate) AND " +
           "(:endDate IS NULL OR a.slotStartTime <= :endDate) AND " +
           "(:appointmentType IS NULL OR a.appointmentType = :appointmentType) AND " +
           "(:providerId IS NULL OR a.provider.id = :providerId) AND " +
           "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.slotStartTime DESC")
    Page<AppointmentSlot> findAppointmentsWithFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("appointmentType") String appointmentType,
            @Param("providerId") UUID providerId,
            @Param("patientId") String patientId,
            @Param("status") String status,
            Pageable pageable);
            
    // Simple query for testing
    @Query("SELECT a FROM AppointmentSlot a")
    Page<AppointmentSlot> findAllSlots(Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM AppointmentSlot a WHERE a.provider.id = :providerId AND a.slotStartTime = :appointmentTime AND a.status = 'BOOKED'")
    long countBookedSlotsByProviderAndTime(
            @Param("providerId") UUID providerId,
            @Param("appointmentTime") LocalDateTime appointmentTime);
    
    @Query("SELECT a FROM AppointmentSlot a WHERE a.bookingReference = :bookingReference")
    Optional<AppointmentSlot> findByBookingReference(@Param("bookingReference") String bookingReference);
    
    @Query("SELECT a FROM AppointmentSlot a WHERE a.availability.id = :availabilityId AND a.status = 'AVAILABLE' ORDER BY a.slotStartTime ASC")
    List<AppointmentSlot> findAvailableSlotsByAvailabilityId(@Param("availabilityId") String availabilityId);
    
    @Query("SELECT COUNT(a) FROM AppointmentSlot a WHERE a.availability.id = :availabilityId AND a.status = 'BOOKED'")
    long countBookedSlotsByAvailabilityId(@Param("availabilityId") String availabilityId);
    
    boolean existsByBookingReference(String bookingReference);
}
