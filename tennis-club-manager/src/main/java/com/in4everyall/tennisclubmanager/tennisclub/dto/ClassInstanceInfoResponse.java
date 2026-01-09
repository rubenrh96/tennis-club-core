package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.ClassInstanceStatus;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ClassInstanceInfoResponse(
        UUID id,
        String classTypeName,  // ej: "Lunes 18:00"
        LocalTime startTime,
        LocalTime endTime,
        ClassInstanceStatus status,
        List<String> playerNames,  // Para admin: lista de jugadores inscritos
        Boolean isIndividual,  // true = clase individual, false = clase grupal
        Integer maxCapacity,  // Capacidad máxima de la clase (1 = individual, >1 = grupal, null = no definido)
        Boolean isConsumed  // true = clase consumida con bono, false = no consumida (solo para jugadores con bonos)
) {}

