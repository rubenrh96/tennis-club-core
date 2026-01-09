package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.List;

public record AdminMatchesSummary(
        String phaseCode,
        int expectedMatches,
        int existingMatches,
        List<MatchResponse> matches
) {}

