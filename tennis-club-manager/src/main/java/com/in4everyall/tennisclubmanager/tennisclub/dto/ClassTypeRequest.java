package com.in4everyall.tennisclubmanager.tennisclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ClassTypeRequest(
        @NotNull(message = "El día de la semana es obligatorio")
        DayOfWeek dayOfWeek,
        
        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,
        
        LocalTime endTime,
        
        String name,
        
        String description,
        
        Integer maxCapacity,
        
        Boolean isActive
) {}

