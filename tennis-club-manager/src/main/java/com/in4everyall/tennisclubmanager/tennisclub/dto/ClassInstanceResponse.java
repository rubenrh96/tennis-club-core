package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.ClassInstanceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ClassInstanceResponse(
        UUID id,
        UUID classTypeId,
        String classTypeName,  // ej: "Lunes 18:00"
        LocalDate date,
        UUID quarterId,
        String quarterName,
        Boolean isHoliday,
        ClassInstanceStatus status,
        String cancellationReason,
        List<String> enrolledPlayerNames  // Para admin: lista de jugadores inscritos
) {}

