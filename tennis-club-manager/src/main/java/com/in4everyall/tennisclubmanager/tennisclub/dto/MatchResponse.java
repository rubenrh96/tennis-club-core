package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;

public record MatchResponse(
        String id,
        Integer groupNo,
        String phaseCode,
        Instant scheduledAt,
        Instant playedAt,
        String player1License,
        String player1Name,
        String player2License,
        String player2Name,
        String winnerLicense,
        String winnerName,
        Short set1P1,
        Short set1P2,
        Short set2P1,
        Short set2P2,
        Short set3P1,
        Short set3P2,
        String submittedByLicense,
        boolean confirmed,
        boolean rejected,
        Boolean cancelled,
        String status
){
    public MatchResponse withGroupNo(Integer newGroupNo) {
        return new MatchResponse(
                id,
                newGroupNo,
                phaseCode,
                scheduledAt,
                playedAt,
                player1License,
                player1Name,
                player2License,
                player2Name,
                winnerLicense,
                winnerName,
                set1P1,
                set1P2,
                set2P1,
                set2P2,
                set3P1,
                set3P2,
                submittedByLicense,
                confirmed,
                rejected,
                cancelled,
                status
        );
    }
}
