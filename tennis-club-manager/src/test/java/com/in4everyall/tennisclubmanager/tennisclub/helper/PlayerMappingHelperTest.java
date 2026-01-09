package com.in4everyall.tennisclubmanager.tennisclub.helper;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerMappingHelperTest {

    private final PlayerMappingHelper helper = new PlayerMappingHelper();

    @Test
    void shouldReturnLicenseNumber() {
        PlayerEntity player = PlayerEntity.builder()
                .licenseNumber("LIC-123")
                .build();

        assertThat(helper.playerToLicense(player)).isEqualTo("LIC-123");
    }

    @Test
    void shouldReturnNullForLicenseWhenPlayerNull() {
        assertThat(helper.playerToLicense(null)).isNull();
    }

    @Test
    void shouldReturnPlayerFullName() {
        PlayerEntity player = PlayerEntity.builder()
                .licenseNumber("LIC-999")
                .user(UserEntity.builder()
                        .email("player@test.com")
                        .firstName("Ana")
                        .lastName("Lopez")
                        .birthDate(java.time.LocalDate.of(1990, 1, 1))
                        .licenseNumber("LIC-999")
                        .passwordHash("pwd")
                        .role(Role.PLAYER)
                        .phone("+34000000000")
                        .build())
                .build();

        assertThat(helper.playerToName(player)).isEqualTo("Ana Lopez");
    }

    @Test
    void shouldReturnNullWhenPlayerHasNoUser() {
        PlayerEntity player = PlayerEntity.builder()
                .licenseNumber("LIC-000")
                .build();

        assertThat(helper.playerToName(player)).isNull();
        assertThat(helper.playerToName(null)).isNull();
    }
}

