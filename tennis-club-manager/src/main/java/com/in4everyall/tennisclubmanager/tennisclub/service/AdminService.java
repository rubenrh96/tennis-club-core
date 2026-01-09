package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AdminMatchesSummary;

public interface AdminService {

    AdminMatchesSummary getMatchesByPhase(String phaseCode);

    void confirmAll(String phaseCode);

    void closePhase(String phaseCode);

    /**
     * Calcula o crea la siguiente fase para el año actual, devolviendo su phaseCode.
     */
    String createOrAdvancePhaseForCurrentYear();

}
