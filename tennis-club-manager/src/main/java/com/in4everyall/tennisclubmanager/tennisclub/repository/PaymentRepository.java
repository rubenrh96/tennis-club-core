package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    
    List<PaymentEntity> findByPlayer_LicenseNumber(String licenseNumber);
    
    List<PaymentEntity> findByPlayer_LicenseNumberOrderByPaymentDateDesc(String licenseNumber);
    
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    List<PaymentEntity> findByPaymentType(PaymentType paymentType);
    
    List<PaymentEntity> findByPlayer_LicenseNumberAndStatus(String licenseNumber, PaymentStatus status);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.player.licenseNumber = :licenseNumber AND p.status = :status ORDER BY p.paymentDate DESC")
    List<PaymentEntity> findPendingPaymentsByPlayer(@Param("licenseNumber") String licenseNumber, @Param("status") PaymentStatus status);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<PaymentEntity> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.player.licenseNumber = :licenseNumber AND p.status = 'PAID'")
    java.math.BigDecimal getTotalPaidByPlayer(@Param("licenseNumber") String licenseNumber);
    
    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.player.licenseNumber = :licenseNumber AND p.status = 'PENDING'")
    java.math.BigDecimal getTotalPendingByPlayer(@Param("licenseNumber") String licenseNumber);
    
    List<PaymentEntity> findBySubscription_Id(UUID subscriptionId);
}




