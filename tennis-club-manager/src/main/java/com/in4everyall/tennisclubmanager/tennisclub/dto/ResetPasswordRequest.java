package com.in4everyall.tennisclubmanager.tennisclub.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword,
        String confirmPassword
) {
}

