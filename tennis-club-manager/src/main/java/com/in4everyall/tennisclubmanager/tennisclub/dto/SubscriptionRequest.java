package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SubscriptionRequest(
        @NotNull(message = "El número de licencia es obligatorio")
        String licenseNumber,
        
        @NotNull(message = "El tipo de suscripción es obligatorio")
        SubscriptionType subscriptionType,
        
        // Para bonos
        Integer classesRemaining,
        LocalDate packagePurchaseDate,
        
        // Para trimestres
        Integer daysPerWeek,
        LocalDate currentQuarterStart,
        LocalDate currentQuarterEnd,
        
        // IDs de tipos de clases para suscripciones trimestrales (opcional)
        List<UUID> classTypeIds,
        
        Boolean autoRenew
) {}




