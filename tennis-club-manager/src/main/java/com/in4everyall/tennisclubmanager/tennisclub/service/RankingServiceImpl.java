package com.in4everyall.tennisclubmanager.tennisclub.service;


import com.in4everyall.tennisclubmanager.tennisclub.dto.StandingRow;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    /**
     * Clasificación completa del grupo del jugador para un mes (phaseMonth),
     * lista ya ordenada e incluyendo la posición (1,2,3...).
     */
    @Transactional(readOnly = true)
    public List<StandingRow> getStandingsForPlayerDashboard(String playerLicenseNumber,
                                                            String phaseCode) {

        // 1. Jugador actual en esa fase
        PlayerEntity currentPlayer = playerRepository
                .findByLicenseNumberAndPhaseCode(playerLicenseNumber, phaseCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player not found for license " + playerLicenseNumber + " and phase " + phaseCode));

        Integer groupNo = currentPlayer.getGroupNo();
        if (groupNo == null) {
            throw new IllegalStateException("Player " + playerLicenseNumber + " has no group assigned for " + phaseCode);
        }

        // 2. Todos los jugadores del mismo grupo y mes
        List<PlayerEntity> groupPlayers =
                playerRepository.findByGroupNoAndPhaseCode(groupNo, phaseCode);

        if (groupPlayers.isEmpty()) {
            return List.of();
        }

        List<String> licenses = groupPlayers.stream()
                .map(PlayerEntity::getLicenseNumber)
                .toList();

        // 3. Partidos confirmados de la fase donde participan jugadores del grupo
        List<MatchEntity> matches =
                matchRepository.findAllConfirmedByPhaseCodeAndPlayers(phaseCode, licenses);

        // 4. Cálculo bruto
        List<MutableStanding> rawStandings = calculateRawStandings(groupPlayers, matches);

        // 5. Ordenar según criterios de desempate
        rawStandings.sort(
                Comparator.comparingInt(MutableStanding::getPoints).reversed()
                        .thenComparingInt(MutableStanding::getSetDifference).reversed()
                        .thenComparingInt(MutableStanding::getGamesDifference).reversed()
                        .thenComparing(MutableStanding::getLicenseNumber)
        );

        // 6. Mapear a DTO añadiendo posición
        List<StandingRow> result = new ArrayList<>();
        for (int i = 0; i < rawStandings.size(); i++) {
            MutableStanding ms = rawStandings.get(i);
            int position = i + 1;
            result.add(new StandingRow(
                    ms.licenseNumber,
                    ms.fullName,
                    ms.groupNo,
                    ms.matchesPlayed,
                    ms.matchesWon,
                    ms.matchesLost,
                    ms.points,
                    ms.setsWon,
                    ms.setsLost,
                    ms.gamesWon,
                    ms.gamesLost,
                    position
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<StandingRow> getStandingsForGroup(Integer groupNo, String phaseCode) {

        // 1) Jugadores del grupo y fase (lógica actual para fase activa)
        List<PlayerEntity> groupPlayers =
                playerRepository.findByGroupNoAndPhaseCode(groupNo, phaseCode);

        if (!groupPlayers.isEmpty()) {
            List<String> licenses = groupPlayers.stream()
                    .map(PlayerEntity::getLicenseNumber)
                    .toList();

            // 2) Partidos confirmados de la fase donde participan estos jugadores
            List<MatchEntity> matches =
                    matchRepository.findAllConfirmedByPhaseCodeAndPlayers(phaseCode, licenses);

            // 3) Cálculo bruto reutilizando tu método interno
            return buildStandings(groupPlayers, matches, null);
        }

        // 2bis) Si no hay jugadores para esa fase/grupo, intentamos modo histórico
        List<MatchEntity> historicalMatches =
                matchRepository.findAllConfirmedByPhaseCodeAndGroup(phaseCode, groupNo);

        if (historicalMatches.isEmpty()) {
            return List.of();
        }

        // Jugadores que realmente han disputado partidos en esa fase y grupo
        var licenses = historicalMatches.stream()
                .flatMap(m -> Stream.of(
                        m.getPlayer1().getLicenseNumber(),
                        m.getPlayer2().getLicenseNumber()
                ))
                .collect(Collectors.toSet());

        List<PlayerEntity> historicalPlayers = playerRepository.findAllById(licenses);
        if (historicalPlayers.isEmpty()) {
            return List.of();
        }

        return buildStandings(historicalPlayers, historicalMatches, groupNo);
    }


    /**
     * Calcula estadísticas PJ/PG/PP, puntos, sets y juegos
     * a partir de jugadores y partidos.
     */
    private List<MutableStanding> calculateRawStandings(List<PlayerEntity> players,
                                                        List<MatchEntity> matches) {

        // Inicializamos stats a 0 para todos los jugadores del grupo
        Map<String, MutableStanding> map = new HashMap<>();
        for (PlayerEntity player : players) {
            String license = player.getLicenseNumber();
            String fullName = Optional.ofNullable(player.getUser())
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse(license); // fallback

            map.put(license, new MutableStanding(
                    license,
                    fullName,
                    player.getGroupNo()
            ));
        }

        // Recorremos los partidos confirmados
        for (MatchEntity match : matches) {

            if (match.getWinner() == null) {
                // por si acaso hubiera alguno confirmado sin ganador
                continue;
            }

            String p1Lic = match.getPlayer1().getLicenseNumber();
            String p2Lic = match.getPlayer2().getLicenseNumber();

            MutableStanding s1 = map.get(p1Lic);
            MutableStanding s2 = map.get(p2Lic);

            // Si por algún motivo el partido es contra alguien de fuera del grupo, lo ignoramos
            if (s1 == null || s2 == null) {
                continue;
            }

            s1.matchesPlayed++;
            s2.matchesPlayed++;

            String winnerLic = match.getWinner().getLicenseNumber();
            boolean p1Wins = winnerLic.equals(p1Lic);

            if (p1Wins) {
                s1.matchesWon++;
                s2.matchesLost++;
                s1.points += 3;
                s2.points += 1;
            } else {
                s2.matchesWon++;
                s1.matchesLost++;
                s2.points += 3;
                s1.points += 1;
            }

            // --- Sets + juegos ---
            // Usamos los sets 1..3 si sus valores no son null.
            // Esto cubre los casos 2-0 y 2-1 que comentabas:
            // - Si no hay tercer set (null), será 2-0.
            // - Si hay tercer set, acabará 2-1.

            accumulateSet(match.getSet1P1(), match.getSet1P2(), s1, s2);
            accumulateSet(match.getSet2P1(), match.getSet2P2(), s1, s2);
            accumulateSet(match.getSet3P1(), match.getSet3P2(), s1, s2);
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Construye la lista de {@link StandingRow} a partir de jugadores y partidos,
     * aplicando el orden de desempate estándar.
     *
     * @param players       jugadores a considerar
     * @param matches       partidos disputados entre esos jugadores
     * @param groupNoFixed  cuando no es null, fuerza ese groupNo en el DTO (útil para histórico);
     *                      si es null se utiliza el groupNo del propio jugador.
     */
    private List<StandingRow> buildStandings(List<PlayerEntity> players,
                                             List<MatchEntity> matches,
                                             Integer groupNoFixed) {

        List<MutableStanding> rawStandings = calculateRawStandings(players, matches);

        rawStandings.sort(
                Comparator.comparingInt(MutableStanding::getPoints).reversed()
                        .thenComparingInt(MutableStanding::getSetDifference).reversed()
                        .thenComparingInt(MutableStanding::getGamesDifference).reversed()
                        .thenComparing(MutableStanding::getLicenseNumber)
        );

        List<StandingRow> result = new ArrayList<>();
        for (int i = 0; i < rawStandings.size(); i++) {
            MutableStanding ms = rawStandings.get(i);
            int position = i + 1;
            Integer groupNoForRow = groupNoFixed != null ? groupNoFixed : ms.groupNo;

            result.add(new StandingRow(
                    ms.licenseNumber,
                    ms.fullName,
                    groupNoForRow,
                    ms.matchesPlayed,
                    ms.matchesWon,
                    ms.matchesLost,
                    ms.points,
                    ms.setsWon,
                    ms.setsLost,
                    ms.gamesWon,
                    ms.gamesLost,
                    position
            ));
        }

        return result;
    }

    /**
     * Acumula juegos y sets para un set concreto (puede ser null si no se jugó).
     */
    private void accumulateSet(Short gamesP1, Short gamesP2,
                               MutableStanding s1,
                               MutableStanding s2) {

        if (gamesP1 == null || gamesP2 == null) {
            return; // set no jugado
        }

        int g1 = gamesP1;
        int g2 = gamesP2;

        // Juegos
        s1.gamesWon += g1;
        s1.gamesLost += g2;

        s2.gamesWon += g2;
        s2.gamesLost += g1;

        // Set ganado/perdido
        if (g1 > g2) {
            s1.setsWon++;
            s2.setsLost++;
        } else if (g2 > g1) {
            s2.setsWon++;
            s1.setsLost++;
        }
        // En teoría no deberías tener empates (6-6) sin tie-break registrado.
    }

    /**
     * Clase interna para ir acumulando estadísticas de forma mutable.
     */
    private static class MutableStanding {
        private final String licenseNumber;
        private final String fullName;
        private final Integer groupNo;

        private int matchesPlayed;
        private int matchesWon;
        private int matchesLost;
        private int points;
        private int setsWon;
        private int setsLost;
        private int gamesWon;
        private int gamesLost;

        private MutableStanding(String licenseNumber, String fullName, Integer groupNo) {
            this.licenseNumber = licenseNumber;
            this.fullName = fullName;
            this.groupNo = groupNo;
        }

        public String getLicenseNumber() {
            return licenseNumber;
        }

        public int getPoints() {
            return points;
        }

        public int getSetDifference() {
            return setsWon - setsLost;
        }

        public int getGamesDifference() {
            return gamesWon - gamesLost;
        }
    }
}

