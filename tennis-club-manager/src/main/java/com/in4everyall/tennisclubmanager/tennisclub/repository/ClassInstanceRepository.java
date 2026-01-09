package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.ClassInstanceEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.ClassInstanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClassInstanceRepository extends JpaRepository<ClassInstanceEntity, UUID> {
    
    List<ClassInstanceEntity> findByQuarter_Id(UUID quarterId);
    
    List<ClassInstanceEntity> findByClassType_Id(UUID classTypeId);
    
    List<ClassInstanceEntity> findByDate(LocalDate date);
    
    List<ClassInstanceEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ClassInstanceEntity> findByStatus(ClassInstanceStatus status);
    
    List<ClassInstanceEntity> findByIsHoliday(Boolean isHoliday);
    
    @Query("SELECT ci FROM ClassInstanceEntity ci WHERE ci.quarter.id = :quarterId AND ci.classType.id = :classTypeId ORDER BY ci.date")
    List<ClassInstanceEntity> findByQuarterAndClassType(@Param("quarterId") UUID quarterId, @Param("classTypeId") UUID classTypeId);
    
    @Query("SELECT ci FROM ClassInstanceEntity ci WHERE ci.date BETWEEN :startDate AND :endDate ORDER BY ci.date, ci.classType.startTime")
    List<ClassInstanceEntity> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ci FROM ClassInstanceEntity ci WHERE ci.classType.id = :classTypeId AND ci.date BETWEEN :startDate AND :endDate ORDER BY ci.date")
    List<ClassInstanceEntity> findByClassTypeAndDateRange(@Param("classTypeId") UUID classTypeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

