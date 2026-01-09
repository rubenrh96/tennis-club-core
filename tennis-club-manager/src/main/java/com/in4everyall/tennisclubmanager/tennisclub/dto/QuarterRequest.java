package com.in4everyall.tennisclubmanager.tennisclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record QuarterRequest(
        @NotNull(message = "El nombre del trimestre es obligatorio")
        String name,
        
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,
        
        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate endDate,
        
        Boolean isActive
) {}

