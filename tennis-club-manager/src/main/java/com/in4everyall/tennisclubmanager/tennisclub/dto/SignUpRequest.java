package com.in4everyall.tennisclubmanager.tennisclub.dto;

public record SignUpRequest(
        String licenseNumber, String firstName, String lastName,
        String birthDate, String forehand, String backhand,
        String email, String passwordHash, String confirmPassword , String role, String phone) {}
