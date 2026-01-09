package com.in4everyall.tennisclubmanager.tennisclub.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateAndExtractEmail() {
        String token = jwtService.generateToken("user@club.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@club.com");
    }
}

