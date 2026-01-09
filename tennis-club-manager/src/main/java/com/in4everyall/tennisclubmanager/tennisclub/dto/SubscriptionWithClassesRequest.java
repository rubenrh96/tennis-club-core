package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SubscriptionWithClassesRequest(
        @NotNull(message = "El número de licencia es obligatorio")
        String licenseNumber,
        
        @NotNull(message = "El tipo de suscripción es obligatorio")
        SubscriptionType subscriptionType,
        
        // Para trimestres: tipos de clases seleccionados
        @NotEmpty(message = "Debe seleccionar al menos un tipo de clase")
        List<UUID> classTypeIds,  // IDs de los tipos de clases del catálogo
        
        // Para bonos
        Integer classesRemaining,
        LocalDate packagePurchaseDate,
        
        // Para trimestres
        @NotNull(message = "La fecha de inicio del trimestre es obligatoria")
        LocalDate currentQuarterStart,
        
        @NotNull(message = "La fecha de fin del trimestre es obligatoria")
        LocalDate currentQuarterEnd,
        
        UUID quarterId,  // ID del trimestre (opcional, se puede calcular desde las fechas)
        
        Boolean autoRenew
) {}

