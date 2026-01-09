package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerClassEnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlayerClassEnrollmentRepository extends JpaRepository<PlayerClassEnrollmentEntity, UUID> {
    
    List<PlayerClassEnrollmentEntity> findByPlayer_LicenseNumber(String licenseNumber);
    
    List<PlayerClassEnrollmentEntity> findByPlayer_LicenseNumberAndIsActive(String licenseNumber, Boolean isActive);
    
    List<PlayerClassEnrollmentEntity> findByClassType_Id(UUID classTypeId);
    
    List<PlayerClassEnrollmentEntity> findBySubscription_Id(UUID subscriptionId);
    
    List<PlayerClassEnrollmentEntity> findByQuarter_Id(UUID quarterId);
    
    @Query("SELECT e FROM PlayerClassEnrollmentEntity e WHERE e.player.licenseNumber = :licenseNumber AND e.quarter.id = :quarterId AND e.isActive = true")
    List<PlayerClassEnrollmentEntity> findByPlayerAndQuarter(@Param("licenseNumber") String licenseNumber, @Param("quarterId") UUID quarterId);
    
    @Query("SELECT e FROM PlayerClassEnrollmentEntity e WHERE e.classType.id = :classTypeId AND e.quarter.id = :quarterId AND e.isActive = true")
    List<PlayerClassEnrollmentEntity> findByClassTypeAndQuarter(@Param("classTypeId") UUID classTypeId, @Param("quarterId") UUID quarterId);
    
    @Query("SELECT COUNT(e) FROM PlayerClassEnrollmentEntity e WHERE e.classType.id = :classTypeId AND e.quarter.id = :quarterId AND e.isActive = true")
    Long countByClassTypeAndQuarter(@Param("classTypeId") UUID classTypeId, @Param("quarterId") UUID quarterId);
}

