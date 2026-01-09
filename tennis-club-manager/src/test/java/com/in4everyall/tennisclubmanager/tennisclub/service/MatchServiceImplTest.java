package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.exception.AddMatchException;
import com.in4everyall.tennisclubmanager.tennisclub.exception.ConfirmMatchException;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.MatchMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.validator.SetValidator;
import com.in4everyall.tennisclubmanager.tennisclub.validator.WinnerValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchMapper matchMapper;
    @Mock
    private PlayerService playerService;
    @Mock
    private SetValidator setValidator;
    @Mock
    private WinnerValidator winnerValidator;

    @InjectMocks
    private MatchServiceImpl service;

    @Test
    void shouldAddMatch_whenRequestValid() {
        var request = request();
        var entity = MatchEntity.builder().build();
        var player1 = PlayerEntity.builder().licenseNumber("P1").phaseCode("2025-1").build();
        var player2 = PlayerEntity.builder().licenseNumber("P2").phaseCode("2025-1").build();
        var winner = PlayerEntity.builder().licenseNumber("P1").build();
        var response = sampleResponse("P1");

        given(matchRepository.existsByPhaseCodeAndPlayers(anyString(), anyString(), anyString())).willReturn(false);
        given(matchMapper.toEntity(request)).willReturn(entity);
        given(playerService.findByLicense("P1")).willReturn(player1);
        given(playerService.findByLicense("P2")).willReturn(player2);
        given(playerService.findOptionalByLicense("P1")).willReturn(winner);
        given(matchRepository.save(entity)).willReturn(entity);
        given(matchMapper.toResponse(entity)).willReturn(response);

        MatchResponse result = service.addMatch(request);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getPlayer1()).isEqualTo(player1);
        assertThat(entity.getPlayer2()).isEqualTo(player2);
        verify(setValidator).validateSets(any(), any(), any(), any(), any(), any());
        verify(winnerValidator).validateWinner(request);
    }

    @Test
    void shouldThrowAddMatchException_whenPlayersAreTheSame() {
        var request = new MatchRequest(
                "2025-1", Instant.now(), Instant.now(),
                "P1", "p1@club.com",
                "P1", "p1@club.com",
                "P1", "p1@club.com",
                (short) 6, (short) 3,
                (short) 6, (short) 3,
                null, null,
                "P1",
                false
        );

        assertThatThrownBy(() -> service.addMatch(request))
                .isInstanceOf(AddMatchException.class);
    }

    @Test
    void shouldConfirmMatch_whenPending() {
        UUID id = UUID.randomUUID();
        var match = MatchEntity.builder()
                .id(id)
                .submittedByLicense("P1")
                .confirmed(false)
                .build();
        var response = sampleResponse("P2");

        given(matchRepository.findById(id)).willReturn(Optional.of(match));
        given(matchRepository.save(match)).willReturn(match);
        given(matchMapper.toResponse(match)).willReturn(response);

        MatchResponse result = service.confirmMatch(id, "P2");

        assertThat(result).isEqualTo(response);
        assertThat(match.isConfirmed()).isTrue();
        assertThat(match.getUpdatedBy()).isEqualTo("P2");
    }

    @Test
    void shouldFailConfirmMatch_whenAlreadyConfirmed() {
        UUID id = UUID.randomUUID();
        var match = MatchEntity.builder()
                .id(id)
                .submittedByLicense("P1")
                .confirmed(true)
                .build();

        given(matchRepository.findById(id)).willReturn(Optional.of(match));

        assertThatThrownBy(() -> service.confirmMatch(id, "P2"))
                .isInstanceOf(ConfirmMatchException.class)
                .hasMessageContaining("ya ha sido confirmado");
    }

    @Test
    void shouldFindMatchesForPlayer() {
        var entity = MatchEntity.builder().id(UUID.randomUUID()).build();
        var response = sampleResponse("P1");
        given(matchRepository.findByPlayerAndPhaseCode("P1", "2025-1")).willReturn(List.of(entity));
        given(matchMapper.toResponse(entity)).willReturn(response);

        var result = service.findMyMatches("P1", "2025-1");

        assertThat(result).containsExactly(response);
    }

    private MatchRequest request() {
        return new MatchRequest(
                "2025-1",
                Instant.now(),
                Instant.now(),
                "P1",
                "p1@club.com",
                "P2",
                "p2@club.com",
                "P1",
                "p1@club.com",
                (short) 6, (short) 3,
                (short) 6, (short) 3,
                null, null,
                "P1",
                false
        );
    }

    private MatchResponse sampleResponse(String winner) {
        return new MatchResponse(
                "id",
                1,
                "2025-1",
                Instant.now(),
                Instant.now(),
                "P1",
                "Player One",
                "P2",
                "Player Two",
                winner,
                "Player Winner",
                (short) 6, (short) 3,
                (short) 6, (short) 3,
                null, null,
                "P1",
                true,
                false,
                false,
                "CONFIRMED"
        );
    }
}

