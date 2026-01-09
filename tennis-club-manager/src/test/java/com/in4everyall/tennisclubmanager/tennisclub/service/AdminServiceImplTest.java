package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AdminMatchesSummary;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.helper.PhaseCodeUtils;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.MatchMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private AdminServiceImpl service;

    @Test
    void shouldBuildSummaryIncludingExpectedMatches() {
        String phaseCode = "2025-1";
        var players = List.of(
                player("P1", 1, phaseCode),
                player("P2", 1, phaseCode),
                player("P3", 1, phaseCode),
                player("P4", 2, phaseCode),
                player("P5", 2, phaseCode)
        );
        var match1 = MatchEntity.builder().id(UUID.randomUUID()).player1(players.get(0)).player2(players.get(1)).build();
        var match2 = MatchEntity.builder().id(UUID.randomUUID()).player1(players.get(3)).player2(players.get(4)).build();

        given(playerRepository.findByPhaseCode(phaseCode)).willReturn(players);
        given(matchRepository.findByPhaseCode(phaseCode)).willReturn(List.of(match1, match2));
        given(matchMapper.toResponse(match1)).willReturn(sampleResponse("P1", "P2"));
        given(matchMapper.toResponse(match2)).willReturn(sampleResponse("P4", "P5"));

        AdminMatchesSummary summary = service.getMatchesByPhase(phaseCode);

        assertThat(summary.expectedMatches()).isEqualTo(4); // 3 en grupo1 + 1 en grupo2
        assertThat(summary.existingMatches()).isEqualTo(2);
        assertThat(summary.matches())
                .extracting(MatchResponse::player1License)
                .containsExactly("P1", "P4");
        assertThat(summary.matches())
                .extracting(MatchResponse::groupNo)
                .containsExactly(1, 2);
    }

    @Test
    void shouldFailToClosePhaseWhenPendingMatches() {
        String phaseCode = "2025-2";
        var unconfirmed = MatchEntity.builder().id(UUID.randomUUID()).confirmed(false).build();
        given(matchRepository.findByPhaseCode(phaseCode)).willReturn(List.of(unconfirmed));

        assertThatThrownBy(() -> service.closePhase(phaseCode))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("sin confirmar");
    }

    @Test
    void shouldCreateFirstPhaseForYearWhenNoneExists() {
        given(playerRepository.findAll()).willReturn(List.of());

        String code = service.createOrAdvancePhaseForYear(2025);

        assertThat(code).isEqualTo("2025-1");
    }

    @Test
    void shouldAdvancePhaseForYearWhenExistingPhasesPresent() {
        var p1 = player("P1", 1, "2025-1");
        var p2 = player("P2", 1, "2025-2");
        var pOldYear = player("P3", 1, "2024-3");
        given(playerRepository.findAll()).willReturn(List.of(p1, p2, pOldYear));

        String code = service.createOrAdvancePhaseForYear(2025);

        assertThat(code).isEqualTo("2025-3");
    }

    private PlayerEntity player(String license, int group, String phaseCode) {
        return PlayerEntity.builder()
                .licenseNumber(license)
                .groupNo(group)
                .phaseCode(phaseCode)
                .user(UserEntity.builder()
                        .licenseNumber(license)
                        .firstName("Name" + license)
                        .lastName("Last" + license)
                        .phone("+34000000000")
                        .email(license.toLowerCase() + "@club.com")
                        .role(Role.PLAYER)
                        .birthDate(null)
                        .build())
                .build();
    }

    private MatchResponse sampleResponse(String p1, String p2) {
        return new MatchResponse(
                UUID.randomUUID().toString(),
                null,
                "2025-1",
                Instant.now(),
                Instant.now(),
                p1,
                "Player " + p1,
                p2,
                "Player " + p2,
                p1,
                "Player " + p1,
                (short) 6, (short) 3,
                (short) 6, (short) 4,
                null, null,
                p1,
                true,
                false,
                false,
                "CONFIRMED"
        );
    }
}

