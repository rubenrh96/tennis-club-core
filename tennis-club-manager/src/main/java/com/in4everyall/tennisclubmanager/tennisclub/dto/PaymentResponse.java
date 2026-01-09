package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String playerLicenseNumber,
        String playerName,
        PaymentType paymentType,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentStatus status,
        Integer classesRemaining,
        LocalDate quarterStartDate,
        LocalDate quarterEndDate,
        Integer daysPerWeek,
        LocalDate classDate,
        String notes,
        UUID subscriptionId  // ID de la suscripción asociada (null si no está vinculado)
) {}



