package com.in4everyall.tennisclubmanager.tennisclub.dto;

public record PlayerUpdateRequest(
        String firstName,
        String lastName,
        Integer groupNo,
        String phone
) {}
