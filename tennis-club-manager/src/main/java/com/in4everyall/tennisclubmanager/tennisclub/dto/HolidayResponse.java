package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.util.UUID;

public record HolidayResponse(
        UUID id,
        LocalDate date,
        String name,
        String region,
        Boolean isNational,
        Integer year
) {}

