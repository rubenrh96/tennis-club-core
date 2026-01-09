package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.StandingRow;
import java.util.List;

public interface RankingService {

    List<StandingRow> getStandingsForPlayerDashboard(String playerLicenseNumber, String phaseCode);

    List<StandingRow> getStandingsForGroup(Integer groupNo, String phaseCode);
}
