package com.in4everyall.tennisclubmanager.tennisclub.dto;


public record StandingRow(
        String licenseNumber,
        String fullName,
        Integer groupNo,
        int matchesPlayed,
        int matchesWon,
        int matchesLost,
        int points,
        int setsWon,
        int setsLost,
        int gamesWon,
        int gamesLost,
        int position // 1 = primero, 2 = segundo, etc.
) {
    public int setDifference() {
        return setsWon - setsLost;
    }

    public int gamesDifference() {
        return gamesWon - gamesLost;
    }
}

