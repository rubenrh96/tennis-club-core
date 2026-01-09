package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.QuarterRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.QuarterResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QuarterService {
    
    QuarterResponse createQuarter(QuarterRequest request);
    
    QuarterResponse getQuarterById(UUID id);
    
    List<QuarterResponse> getAllQuarters();
    
    QuarterResponse getActiveQuarter();
    
    QuarterResponse getQuarterByDate(LocalDate date);
    
    QuarterResponse updateQuarter(UUID id, QuarterRequest request);
    
    void deleteQuarter(UUID id);
}

