package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.HolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<HolidayEntity, UUID> {
    
    Optional<HolidayEntity> findByDate(LocalDate date);
    
    List<HolidayEntity> findByYear(Integer year);
    
    List<HolidayEntity> findByRegion(String region);
    
    List<HolidayEntity> findByYearAndRegion(Integer year, String region);
    
    @Query("SELECT h FROM HolidayEntity h WHERE h.date BETWEEN :startDate AND :endDate ORDER BY h.date")
    List<HolidayEntity> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    boolean existsByDate(LocalDate date);
}

