package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.StandingRow;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private RankingServiceImpl service;

    @Test
    void shouldComputeStandingsForPlayerDashboard() {
        String phaseCode = "2025-1";
        var player1 = player("P1", "Ana", "López", 1, phaseCode);
        var player2 = player("P2", "Beto", "Ruiz", 1, phaseCode);

        given(playerRepository.findByLicenseNumberAndPhaseCode("P1", phaseCode))
                .willReturn(Optional.of(player1));
        given(playerRepository.findByGroupNoAndPhaseCode(1, phaseCode))
                .willReturn(List.of(player1, player2));
        given(matchRepository.findAllConfirmedByPhaseCodeAndPlayers(eq(phaseCode), anyList()))
                .willReturn(List.of(match(player1, player2, player1)));

        List<StandingRow> rows = service.getStandingsForPlayerDashboard("P1", phaseCode);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).licenseNumber()).isEqualTo("P1");
        assertThat(rows.get(0).points()).isEqualTo(3);
        assertThat(rows.get(1).points()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyStandingsWhenGroupEmpty() {
        String phaseCode = "2025-2";
        given(playerRepository.findByGroupNoAndPhaseCode(2, phaseCode)).willReturn(List.of());
        given(matchRepository.findAllConfirmedByPhaseCodeAndGroup(phaseCode, 2)).willReturn(List.of());

        List<StandingRow> rows = service.getStandingsForGroup(2, phaseCode);

        assertThat(rows).isEmpty();
    }

    @Test
    void shouldComputeHistoricalStandingsWhenNoPlayersForPhase() {
        String phaseCode = "2025-3";
        var player1 = player("P1", "Ana", "López", 99, phaseCode); // groupNo actual irrelevante
        var player2 = player("P2", "Beto", "Ruiz", 88, phaseCode);

        // No hay jugadores con ese phaseCode/grupo registrados (fase histórica)
        given(playerRepository.findByGroupNoAndPhaseCode(1, phaseCode)).willReturn(List.of());

        MatchEntity match = MatchEntity.builder()
                .id(UUID.randomUUID())
                .phaseCode(phaseCode)
                .groupNoAtMatch(1)
                .player1(player1)
                .player2(player2)
                .winner(player1)
                .set1P1((short) 6)
                .set1P2((short) 4)
                .set2P1((short) 6)
                .set2P2((short) 3)
                .build();

        given(matchRepository.findAllConfirmedByPhaseCodeAndGroup(phaseCode, 1))
                .willReturn(List.of(match));

        given(playerRepository.findAllById(anyCollection())).willAnswer(invocation -> {
            var ids = (java.util.Collection<String>) invocation.getArgument(0);
            if (ids.contains("P1") && ids.contains("P2")) {
                return List.of(player1, player2);
            }
            return List.of();
        });

        List<StandingRow> rows = service.getStandingsForGroup(1, phaseCode);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).licenseNumber()).isEqualTo("P1");
        assertThat(rows.get(0).groupNo()).isEqualTo(1); // group del histórico
        assertThat(rows.get(0).points()).isGreaterThan(rows.get(1).points());
    }

    private PlayerEntity player(String license, String first, String last, int group, String phaseCode) {
        return PlayerEntity.builder()
                .licenseNumber(license)
                .groupNo(group)
                .phaseCode(phaseCode)
                .user(UserEntity.builder()
                        .licenseNumber(license)
                        .firstName(first)
                        .lastName(last)
                        .role(Role.PLAYER)
                        .phone("+34000000000")
                        .email(first.toLowerCase() + "@club.com")
                        .build())
                .build();
    }

    private MatchEntity match(PlayerEntity p1, PlayerEntity p2, PlayerEntity winner) {
        return MatchEntity.builder()
                .id(UUID.randomUUID())
                .player1(p1)
                .player2(p2)
                .winner(winner)
                .set1P1((short) 6)
                .set1P2((short) 4)
                .set2P1((short) 6)
                .set2P2((short) 4)
                .build();
    }
}

