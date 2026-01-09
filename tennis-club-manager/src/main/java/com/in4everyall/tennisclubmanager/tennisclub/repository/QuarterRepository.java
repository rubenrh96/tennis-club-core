package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.QuarterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuarterRepository extends JpaRepository<QuarterEntity, UUID> {
    
    Optional<QuarterEntity> findByName(String name);
    
    List<QuarterEntity> findByIsActive(Boolean isActive);
    
    @Query("SELECT q FROM QuarterEntity q WHERE q.isActive = true ORDER BY q.startDate DESC")
    Optional<QuarterEntity> findActiveQuarter();
    
    @Query("SELECT q FROM QuarterEntity q WHERE :date BETWEEN q.startDate AND q.endDate")
    Optional<QuarterEntity> findByDate(@Param("date") LocalDate date);
    
    @Query("SELECT q FROM QuarterEntity q WHERE q.startDate <= :endDate AND q.endDate >= :startDate")
    List<QuarterEntity> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<QuarterEntity> findByStartDateBetween(LocalDate start, LocalDate end);
}

