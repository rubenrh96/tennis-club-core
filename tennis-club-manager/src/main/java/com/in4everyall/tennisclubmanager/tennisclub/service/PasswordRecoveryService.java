package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PasswordResetToken;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PasswordResetTokenRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.token-validity-minutes:60}")
    private long tokenValidityMinutes;

    @Value("${app.password-reset.frontend-url:https://tennisclubmanager.com/reset-password}")
    private String frontendResetUrl;

    public PasswordRecoveryService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void initiatePasswordReset(String email, String licenseNumber) {
        if (email == null || email.isBlank() || licenseNumber == null || licenseNumber.isBlank()) {
            return;
        }

        Optional<UserEntity> userOpt = userRepository.findByEmailAndLicenseNumber(email, licenseNumber);
        if (userOpt.isEmpty()) {
            // No revelar si el usuario existe o no
            return;
        }

        UserEntity user = userOpt.get();

        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(tokenValidityMinutes));

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setUsed(false);
        tokenRepository.save(token);

        String resetUrl = frontendResetUrl + "?token=" + tokenValue;
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    public void resetPassword(String tokenValue, String newPassword, String confirmPassword) {
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(BAD_REQUEST, "Las contraseñas no coinciden");
        }

        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Token de recuperación inválido"));

        if (token.isUsed()) {
            throw new ResponseStatusException(BAD_REQUEST, "El enlace de recuperación ya ha sido utilizado");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "El enlace de recuperación ha caducado");
        }

        UserEntity user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }
}

