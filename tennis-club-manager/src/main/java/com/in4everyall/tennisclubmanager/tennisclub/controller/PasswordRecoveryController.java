package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ForgotPasswordRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ResetPasswordRequest;
import com.in4everyall.tennisclubmanager.tennisclub.service.PasswordRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordRecoveryService.initiatePasswordReset(request.email(), request.licenseNumber());
        return ResponseEntity.ok(
                Map.of("message",
                        "Si existe una cuenta con esos datos, hemos enviado instrucciones para restablecer la contraseña.")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordRecoveryService.resetPassword(
                request.token(),
                request.newPassword(),
                request.confirmPassword()
        );
        return ResponseEntity.ok(
                Map.of("message", "La contraseña se ha restablecido correctamente.")
        );
    }
}

