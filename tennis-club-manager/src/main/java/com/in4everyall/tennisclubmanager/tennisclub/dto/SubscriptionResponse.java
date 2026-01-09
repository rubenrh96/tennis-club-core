package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        String licenseNumber,
        String playerName,
        SubscriptionType subscriptionType,
        Boolean isActive,
        Integer classesRemaining,
        Integer daysPerWeek,
        LocalDate currentQuarterStart,
        LocalDate currentQuarterEnd,
        BigDecimal monthlyCost,
        Boolean autoRenew
) {}




