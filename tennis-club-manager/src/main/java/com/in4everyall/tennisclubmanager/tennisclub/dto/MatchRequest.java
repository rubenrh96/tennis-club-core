package com.in4everyall.tennisclubmanager.tennisclub.dto;

import org.hibernate.validator.constraints.UUID;

import java.time.Instant;

public record MatchRequest(
        String phaseCode,
        Instant scheduledAt,
        Instant playedAt,
        String player1License,
        String player1Email,
        String player2License,
        String player2Email,
        String winnerLicense,
        String winnerEmail,
        Short set1P1,
        Short set1P2,
        Short set2P1,
        Short set2P2,
        Short set3P1,
        Short set3P2,
        String submittedByLicense,
        boolean confirmed
) {}
