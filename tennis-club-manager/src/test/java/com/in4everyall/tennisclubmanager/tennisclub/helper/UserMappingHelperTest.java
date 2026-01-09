package com.in4everyall.tennisclubmanager.tennisclub.helper;

import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserMappingHelperTest {

    private final UserMappingHelper helper = new UserMappingHelper();

    @Test
    void shouldConvertStringToLocalDate() {
        LocalDate date = helper.stringToLocalDate("2025-01-15");

        assertThat(date).isEqualTo(LocalDate.of(2025, 1, 15));
    }

    @Test
    void shouldReturnNullWhenDateIsBlank() {
        assertThat(helper.stringToLocalDate(null)).isNull();
        assertThat(helper.stringToLocalDate("   ")).isNull();
    }

    @Test
    void shouldConvertStringToRole() {
        assertThat(helper.stringToRole("admin")).isEqualTo(Role.ADMIN);
        assertThat(helper.stringToRole("PLAYER")).isEqualTo(Role.PLAYER);
    }

    @Test
    void shouldDefaultRoleWhenBlank() {
        assertThat(helper.stringToRole(null)).isEqualTo(Role.PLAYER);
        assertThat(helper.stringToRole("  ")).isEqualTo(Role.PLAYER);
    }
}

