package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassInstanceResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClassInstanceService {
    
    List<ClassInstanceResponse> generateInstancesForSubscription(
            UUID subscriptionId,
            List<UUID> classTypeIds,
            LocalDate quarterStart,
            LocalDate quarterEnd
    );
    
    ClassInstanceResponse getClassInstanceById(UUID id);
    
    List<ClassInstanceResponse> getClassInstancesByQuarter(UUID quarterId);
    
    List<ClassInstanceResponse> getClassInstancesByClassType(UUID classTypeId);
    
    CalendarResponse getCalendarForPlayer(String licenseNumber, LocalDate startDate, LocalDate endDate);
    
    CalendarResponse getCalendarForAdmin(LocalDate startDate, LocalDate endDate, UUID quarterId, UUID classTypeId);
    
    ClassInstanceResponse cancelClassInstance(UUID id, String reason);
    
    ClassInstanceResponse completeClassInstance(UUID id);
    
    void deleteClassInstance(UUID id);
}

