package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentSummaryResponse(
        BigDecimal totalPaid,
        BigDecimal totalPending,
        Integer pendingPaymentsCount,
        List<PaymentByTypeSummary> byType
) {}




