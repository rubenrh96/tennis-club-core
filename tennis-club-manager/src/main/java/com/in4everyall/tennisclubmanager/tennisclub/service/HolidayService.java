package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.HolidayRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.HolidayResponse;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    
    HolidayResponse createHoliday(HolidayRequest request);
    
    HolidayResponse getHolidayById(java.util.UUID id);
    
    List<HolidayResponse> getAllHolidays();
    
    List<HolidayResponse> getHolidaysByYear(Integer year);
    
    List<HolidayResponse> getHolidaysByRegion(String region);
    
    List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);
    
    boolean isHoliday(LocalDate date);
    
    void syncHolidaysFromAPI(Integer year);
    
    void deleteHoliday(java.util.UUID id);
}

