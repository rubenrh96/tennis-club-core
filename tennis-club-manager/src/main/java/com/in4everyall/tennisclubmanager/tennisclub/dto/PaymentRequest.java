package com.in4everyall.tennisclubmanager.tennisclub.dto;

import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotNull(message = "El número de licencia es obligatorio")
        String licenseNumber,
        
        @NotNull(message = "El tipo de pago es obligatorio")
        PaymentType paymentType,
        
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser positivo")
        BigDecimal amount,
        
        @NotNull(message = "La fecha de pago es obligatoria")
        LocalDate paymentDate,
        
        // Para clases individuales
        LocalDate classDate,
        
        // Para trimestres
        Integer daysPerWeek,
        Integer year,
        Integer quarterNumber,
        
        String notes
) {}

