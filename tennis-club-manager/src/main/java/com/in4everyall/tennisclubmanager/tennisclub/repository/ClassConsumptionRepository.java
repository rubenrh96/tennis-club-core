package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.ClassConsumptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClassConsumptionRepository extends JpaRepository<ClassConsumptionEntity, UUID> {
    
    List<ClassConsumptionEntity> findBySubscription_Id(UUID subscriptionId);
    
    List<ClassConsumptionEntity> findByPlayer_LicenseNumber(String licenseNumber);
    
    @Query("SELECT c FROM ClassConsumptionEntity c WHERE c.subscription.id = :subscriptionId ORDER BY c.classDate DESC, c.classTime DESC")
    List<ClassConsumptionEntity> findBySubscriptionOrdered(@Param("subscriptionId") UUID subscriptionId);
    
    @Query("SELECT c FROM ClassConsumptionEntity c WHERE c.player.licenseNumber = :licenseNumber ORDER BY c.classDate DESC")
    List<ClassConsumptionEntity> findByPlayerOrdered(@Param("licenseNumber") String licenseNumber);
    
    @Query("SELECT COUNT(c) FROM ClassConsumptionEntity c WHERE c.subscription.id = :subscriptionId")
    Long countBySubscription(@Param("subscriptionId") UUID subscriptionId);
    
    @Query("SELECT c FROM ClassConsumptionEntity c WHERE c.subscription.id = :subscriptionId AND c.classDate BETWEEN :startDate AND :endDate")
    List<ClassConsumptionEntity> findBySubscriptionAndDateRange(@Param("subscriptionId") UUID subscriptionId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

