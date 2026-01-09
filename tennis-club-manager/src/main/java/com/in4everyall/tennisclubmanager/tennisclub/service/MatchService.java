package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import java.util.*;

public interface MatchService {

    List<MatchResponse> findMyMatches(String license, String phaseCode);

    MatchResponse addMatch(MatchRequest matchRequest);

    MatchResponse confirmMatch(UUID matchId, String currentLicense);

    boolean existsByPhaseAndPlayers(String phaseCode, String p1, String p2);
    
    MatchResponse rejectMatch(UUID matchId, String rejecterLicense);

    MatchResponse adminAddMatch(String adminLicense, MatchRequest request);

    /**
     * Todos los códigos de fase conocidos por el sistema (para administración).
     */
    List<String> getAllPhaseCodesForAdmin();

    /**
     * Códigos de fase en los que un jugador tiene al menos un partido.
     */
    List<String> getPhaseCodesForPlayer(String license);

    /**
     * Todos los partidos de un jugador en todas las fases.
     */
    List<MatchResponse> findAllMyMatches(String license);

    /**
     * Cancela un partido (solo administradores).
     */
    MatchResponse cancelMatch(UUID matchId, String adminLicense);
}
