package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.ClassTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface ClassTypeRepository extends JpaRepository<ClassTypeEntity, UUID> {
    
    List<ClassTypeEntity> findByIsActive(Boolean isActive);
    
    List<ClassTypeEntity> findByDayOfWeek(DayOfWeek dayOfWeek);
    
    List<ClassTypeEntity> findByDayOfWeekAndIsActive(DayOfWeek dayOfWeek, Boolean isActive);
    
    @Query("SELECT ct FROM ClassTypeEntity ct WHERE ct.isActive = true ORDER BY ct.dayOfWeek, ct.startTime")
    List<ClassTypeEntity> findAllActiveOrdered();
    
    @Query("SELECT ct FROM ClassTypeEntity ct WHERE ct.dayOfWeek = :dayOfWeek AND ct.isActive = true ORDER BY ct.startTime")
    List<ClassTypeEntity> findByDayOfWeekActive(@Param("dayOfWeek") DayOfWeek dayOfWeek);
}

