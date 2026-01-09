package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record ClassTypeResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String name,
        String description,
        Integer maxCapacity,
        Boolean isActive,
        Long enrolledPlayersCount  // Número de jugadores inscritos (calculado)
) {}

