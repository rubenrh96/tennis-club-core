package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;

public record PlayerGroupItemResponse(
        String license,
        String fullName,
        Integer groupNo,
        String phaseCode,
        String phone
) {}
