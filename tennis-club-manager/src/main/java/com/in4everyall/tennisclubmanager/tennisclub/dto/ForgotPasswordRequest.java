package com.in4everyall.tennisclubmanager.tennisclub.dto;

public record ForgotPasswordRequest(
        String email,
        String licenseNumber
) {
}

