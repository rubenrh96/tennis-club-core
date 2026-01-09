package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ClassConsumptionResponse(
        UUID id,
        String playerLicenseNumber,
        String playerName,
        UUID subscriptionId,
        LocalDate classDate,
        LocalTime classTime,
        UUID classTypeId,
        String classTypeName,
        String consumedBy,
        Integer classesRemaining  // Clases restantes después de este consumo
) {}

