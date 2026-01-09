package com.in4everyall.tennisclubmanager.tennisclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayRequest(
        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,
        
        @NotNull(message = "El nombre del festivo es obligatorio")
        String name,
        
        String region,
        
        Boolean isNational
) {}

