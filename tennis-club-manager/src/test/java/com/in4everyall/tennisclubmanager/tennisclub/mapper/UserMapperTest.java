package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.helper.UserMappingHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {UserMapperImpl.class, UserMappingHelper.class})
class UserMapperTest {

    @Autowired
    private UserMapper mapper;

    @Test
    void shouldMapSignUpRequestToEntity() {
        var request = new SignUpRequest(
                "LIC-123",
                "Ana",
                "García",
                "1992-05-21",
                "top",
                "twoHanded",
                "ana@club.com",
                "plainSecret",
                "plainSecret",
                "ADMIN",
                "+34600111222"
        );

        UserEntity entity = mapper.toEntity(request);

        assertThat(entity.getLicenseNumber()).isEqualTo("LIC-123");
        assertThat(entity.getFirstName()).isEqualTo("Ana");
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1992, 5, 21));
        assertThat(entity.getRole()).isEqualTo(Role.ADMIN);
        assertThat(entity.getPhone()).isEqualTo("+34600111222");
    }

    @Test
    void shouldMapUserToResponseWithToken() {
        var user = UserEntity.builder()
                .licenseNumber("LIC-999")
                .firstName("Luis")
                .lastName("Pérez")
                .birthDate(LocalDate.of(1990, 1, 15))
                .email("luis@club.com")
                .role(Role.PLAYER)
                .phone("+34123456789")
                .build();

        UserResponse response = mapper.toResponse(user, "token-123");

        assertThat(response.licenseNumber()).isEqualTo("LIC-999");
        assertThat(response.role()).isEqualTo("PLAYER");
        assertThat(response.token()).isEqualTo("token-123");
    }
}

