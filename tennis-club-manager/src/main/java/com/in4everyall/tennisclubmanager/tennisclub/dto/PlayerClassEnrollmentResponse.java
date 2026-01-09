package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record PlayerClassEnrollmentResponse(
        UUID id,
        String playerLicenseNumber,
        String playerName,
        UUID classTypeId,
        String classTypeName,  // ej: "Lunes 18:00"
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        UUID subscriptionId,
        UUID quarterId,
        String quarterName,
        Boolean isActive
) {}

