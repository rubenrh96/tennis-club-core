package com.in4everyall.tennisclubmanager.tennisclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ClassConsumptionRequest(
        @NotNull(message = "El ID de la suscripción es obligatorio")
        UUID subscriptionId,
        
        @NotNull(message = "La fecha de la clase es obligatoria")
        LocalDate classDate,
        
        LocalTime classTime,
        
        UUID classTypeId  // Opcional
) {}

