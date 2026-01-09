package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<CalendarDayResponse> days
) {}

