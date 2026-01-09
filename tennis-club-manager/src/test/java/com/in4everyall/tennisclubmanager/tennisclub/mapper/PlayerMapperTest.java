package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = PlayerMapperImpl.class)
class PlayerMapperTest {

    @Autowired
    private PlayerMapper mapper;

    @Test
    void shouldMapSignUpRequestToPlayerEntity() {
        var request = new SignUpRequest(
                "LIC-777",
                "Eva",
                "López",
                "1995-08-01",
                "top",
                "slice",
                "eva@club.com",
                "secretPwd",
                "secretPwd",
                "PLAYER",
                "+34999000111"
        );

        PlayerEntity entity = mapper.toEntity(request);

        assertThat(entity.getLicenseNumber()).isEqualTo("LIC-777");
        assertThat(entity.getForehand()).isEqualTo("top");
        assertThat(entity.getBackhand()).isEqualTo("slice");
    }

    @Test
    void shouldMapPlayerToGroupItemResponse() {
        var player = PlayerEntity.builder()
                .licenseNumber("LIC-888")
                .groupNo(3)
                .phaseCode("2025-2")
                .user(UserEntity.builder()
                        .licenseNumber("LIC-888")
                        .firstName("Mario")
                        .lastName("Rossi")
                        .role(Role.PLAYER)
                        .phone("+34000111222")
                        .email("mario@club.com")
                        .birthDate(null)
                        .build())
                .build();

        PlayerGroupItemResponse response = mapper.toGroupItemResponse(player);

        assertThat(response.license()).isEqualTo("LIC-888");
        assertThat(response.groupNo()).isEqualTo(3);
        assertThat(response.fullName()).isEqualTo("Mario Rossi");
        assertThat(response.phaseCode()).isEqualTo("2025-2");
        assertThat(response.phone()).isEqualTo("+34000111222");
    }
}

