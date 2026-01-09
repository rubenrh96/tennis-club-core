package com.in4everyall.tennisclubmanager.tennisclub.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BulkSubscriptionRequest(
        @NotNull(message = "El ID del trimestre es obligatorio")
        UUID quarterId,
        
        @NotNull(message = "El ID del tipo de clase es obligatorio")
        UUID classTypeId,
        
        @NotEmpty(message = "Debe seleccionar al menos un jugador")
        List<String> licenseNumbers,  // Lista de licencias de jugadores
        
        @NotNull(message = "La fecha de inicio del trimestre es obligatoria")
        LocalDate quarterStartDate,
        
        @NotNull(message = "La fecha de fin del trimestre es obligatoria")
        LocalDate quarterEndDate
) {}

