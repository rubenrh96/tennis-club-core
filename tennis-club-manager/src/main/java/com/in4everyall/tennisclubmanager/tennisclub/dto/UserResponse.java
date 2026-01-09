package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;

public record UserResponse(
        String licenseNumber, String firstName, String lastName,
        LocalDate birthDate, String email, String role, String phone, String token
) {}
