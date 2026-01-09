package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.util.UUID;

public record QuarterResponse(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive
) {}

