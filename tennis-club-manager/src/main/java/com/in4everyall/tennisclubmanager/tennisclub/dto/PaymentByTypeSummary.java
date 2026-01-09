package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;

import java.math.BigDecimal;

public record PaymentByTypeSummary(
        PaymentType type,
        BigDecimal totalPaid,
        BigDecimal totalPending,
        Integer count
) {}




