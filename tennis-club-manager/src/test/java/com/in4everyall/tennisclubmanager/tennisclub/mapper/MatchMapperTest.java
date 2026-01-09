package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.helper.PlayerMappingHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {MatchMapperImpl.class, PlayerMappingHelper.class})
class MatchMapperTest {

    @Autowired
    private MatchMapper mapper;

    @Test
    void shouldConvertRequestToEntityIgnoringPlayers() {
        var request = new MatchRequest(
                "2025-1",
                Instant.parse("2025-03-10T10:00:00Z"),
                Instant.parse("2025-03-11T10:00:00Z"),
                "P1",
                "p1@club.com",
                "P2",
                "p2@club.com",
                "P1",
                "p1@club.com",
                (short) 6, (short) 4,
                (short) 6, (short) 3,
                null, null,
                "P1",
                true
        );

        MatchEntity entity = mapper.toEntity(request);

        assertThat(entity.getPhaseCode()).isEqualTo("2025-1");
        assertThat(entity.getSubmittedByLicense()).isEqualTo("P1");
        assertThat(entity.getPlayer1()).isNull();
        assertThat(entity.getPlayer2()).isNull();
    }

    @Test
    void shouldConvertEntityToResponseUsingHelper() {
        var player1 = player("P1", "Ana", "López");
        var player2 = player("P2", "Clara", "Ruiz");
        var winner = player1;

        var entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .phaseCode("2025-3")
                .player1(player1)
                .player2(player2)
                .winner(winner)
                .set1P1((short) 6).set1P2((short) 4)
                .set2P1((short) 6).set2P2((short) 3)
                .submittedByLicense("P1")
                .confirmed(true)
                .rejected(false)
                .cancelled(false)
                .build();

        MatchResponse response = mapper.toResponse(entity);

        assertThat(response.player1Name()).isEqualTo("Ana López");
        assertThat(response.player2License()).isEqualTo("P2");
        assertThat(response.winnerName()).isEqualTo("Ana López");
        assertThat(response.confirmed()).isTrue();
    }

    private PlayerEntity player(String license, String firstName, String lastName) {
        return PlayerEntity.builder()
                .licenseNumber(license)
                .user(UserEntity.builder()
                        .licenseNumber(license)
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(firstName.toLowerCase() + "@club.com")
                        .role(Role.PLAYER)
                        .phone("+34000000000")
                        .birthDate(null)
                        .build())
                .build();
    }
}

