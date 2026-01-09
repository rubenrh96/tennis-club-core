package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassTypeRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassTypeResponse;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface ClassTypeService {
    
    ClassTypeResponse createClassType(ClassTypeRequest request);
    
    ClassTypeResponse getClassTypeById(UUID id);
    
    List<ClassTypeResponse> getAllClassTypes();
    
    List<ClassTypeResponse> getActiveClassTypes();
    
    List<ClassTypeResponse> getClassTypesByDayOfWeek(DayOfWeek dayOfWeek);
    
    ClassTypeResponse updateClassType(UUID id, ClassTypeRequest request);
    
    void deleteClassType(UUID id);
}

