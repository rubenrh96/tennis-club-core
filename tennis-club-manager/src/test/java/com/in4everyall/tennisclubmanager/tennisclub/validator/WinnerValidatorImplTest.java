package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.exception.SetException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WinnerValidatorImplTest {

    private final WinnerValidatorImpl validator = new WinnerValidatorImpl();

    @Test
    void shouldAcceptWinnerMatchingScores() {
        assertThatCode(() -> validator.validateWinner(request("P1", (short) 6, (short) 4, (short) 6, (short) 3)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenWinnerDoesNotMatchScores() {
        assertThatThrownBy(() -> validator.validateWinner(request("P2", (short) 6, (short) 4, (short) 6, (short) 3)))
                .isInstanceOf(SetException.class)
                .hasMessageContaining("ganador indicado");
    }

    private MatchRequest request(String winner, Short set1P1, Short set1P2, Short set2P1, Short set2P2) {
        return new MatchRequest(
                "2025-5",
                Instant.now(),
                Instant.now(),
                "P1",
                "p1@club.com",
                "P2",
                "p2@club.com",
                winner,
                winner.equals("P1") ? "p1@club.com" : "p2@club.com",
                set1P1, set1P2,
                set2P1, set2P2,
                null, null,
                "P1",
                false
        );
    }
}

