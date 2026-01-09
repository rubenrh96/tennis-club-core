package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayResponse(
        LocalDate date,
        List<ClassInstanceInfoResponse> classes,
        Boolean isHoliday,
        String holidayName,
        Boolean isCancelled,
        String cancellationReason
) {}

