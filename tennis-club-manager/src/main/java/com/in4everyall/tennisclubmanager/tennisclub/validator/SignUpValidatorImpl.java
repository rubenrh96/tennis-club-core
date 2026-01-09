package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class SignUpValidatorImpl implements SignUpValidator {

    public void validateSignUpForm(SignUpRequest req){

    if (req.licenseNumber() == null || req.licenseNumber().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El número de licencia (DNI) es obligatorio"
        );
    }

    if (!isValidDni(req.licenseNumber())) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El DNI no es válido"
        );
    }

    if (req.firstName() == null || req.firstName().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nombre es obligatorio"
        );
    }

    if (req.lastName() == null || req.lastName().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Los apellidos son obligatorios"
        );
    }

    if (req.birthDate() == null || req.birthDate().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha de nacimiento es obligatoria"
        );
    }
    try {
        LocalDate birth = LocalDate.parse(req.birthDate());
        if (birth.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser futura"
            );
        }
    } catch (DateTimeParseException e) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha de nacimiento tiene un formato inválido"
        );
    }

    if (req.email() == null || req.email().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El email es obligatorio"
        );
    }
    if (!req.email().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El email no tiene un formato válido"
        );
    }

    if (req.passwordHash() == null || req.passwordHash().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La contraseña es obligatoria"
        );
    }
    if (req.passwordHash().length() < 8) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La contraseña debe tener al menos 8 caracteres"
        );
    }

    if (req.confirmPassword() == null || req.confirmPassword().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes repetir la contraseña"
        );
    }
    if (!req.passwordHash().equals(req.confirmPassword())) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las contraseñas no coinciden"
        );
    }

    if (req.phone() == null || req.phone().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El teléfono es obligatorio"
        );
    }
    if (!req.phone().matches("^\\+?[0-9]{7,15}$")) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El teléfono debe tener entre 7 y 15 dígitos e incluir el prefijo"
        );
    }
}

private boolean isValidDni(String raw) {
    if (raw == null) {
        return false;
    }

    String dni = raw.trim().toUpperCase();

    if (!dni.matches("^\\d{8}[A-Z]$")) {
        return false;
    }

    String numberPart = dni.substring(0, 8);
    char letter = dni.charAt(8);

    int num = Integer.parseInt(numberPart);
    String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
    char expected = letters.charAt(num % 23);

    return letter == expected;
    }
}
