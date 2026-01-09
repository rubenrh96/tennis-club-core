package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AdminMatchesSummary;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.MatchMapper;
import com.in4everyall.tennisclubmanager.tennisclub.helper.PhaseCodeUtils;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    @Override
    public AdminMatchesSummary getMatchesByPhase(String phaseCode) {
        List<PlayerEntity> players = playerRepository.findByPhaseCode(phaseCode);
        Map<Integer, List<PlayerEntity>> byGroup = players.stream()
                .collect(Collectors.groupingBy(PlayerEntity::getGroupNo));

        Map<String, Integer> groupByLicense = players.stream()
                .collect(Collectors.toMap(
                        PlayerEntity::getLicenseNumber, // clave → nº licencia del jugador
                        PlayerEntity::getGroupNo       // valor → groupNo del jugador
                ));


        int expected = 0;
        for (List<PlayerEntity> groupPlayers : byGroup.values()) {
            int n = groupPlayers.size();
            expected += n * (n - 1) / 2;
        }

        List<MatchEntity> matches = matchRepository.findByPhaseCode(phaseCode);
        List<MatchResponse> matchDtos = matches.stream()
                .map(m -> {
                    MatchResponse dto = matchMapper.toResponse(m);

                    // 1) Preferimos siempre el grupo histórico guardado en el propio partido
                    Integer groupNo = m.getGroupNoAtMatch();

                    // 2) Fallback para partidos antiguos sin groupNoAtMatch:
                    //    inferimos el grupo a partir de los jugadores de esa fase (si hay)
                    if (groupNo == null) {
                        groupNo = groupByLicense.get(dto.player1License());
                        if (groupNo == null) {
                            groupNo = groupByLicense.get(dto.player2License());
                        }
                    }

                    return dto.withGroupNo(groupNo);
                })
                .collect(Collectors.toList());

        return new AdminMatchesSummary(
                phaseCode,
                expected,
                matches.size(),
                matchDtos
        );
    }

    @Override
    public void confirmAll(String phaseCode) {
        List<MatchEntity> matches = matchRepository.findByPhaseCode(phaseCode);
        matches.forEach(m -> {
            m.setConfirmed(true);
        });
        matchRepository.saveAll(matches);
    }

    @Override
    @Transactional
    public void closePhase(String phaseCodeToClose) {

        List<MatchEntity> matches = matchRepository.findByPhaseCode(phaseCodeToClose);

        boolean allConfirmed = matches.stream().allMatch(MatchEntity::isConfirmed);

        if (!allConfirmed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cerrar el mes: todavía hay partidos sin confirmar."
            );
        }

        Map<Integer, List<PlayerEntity>> grupos =
                playerRepository.findAll().stream()
                        .collect(Collectors.groupingBy(PlayerEntity::getGroupNo));

        actualizarGrupos(grupos, phaseCodeToClose);

        // Crear o avanzar fase para el año actual y asignarla como fase activa a todos los jugadores
        String nextPhaseCode = createOrAdvancePhaseForCurrentYear();

        for (PlayerEntity p : playerRepository.findAll()) {
            p.setPhaseCode(nextPhaseCode);
            playerRepository.save(p);
        }
    }

    private void actualizarGrupos(Map<Integer, List<PlayerEntity>> grupos, String phaseCode) {

        int maxGroup = grupos.keySet().stream().max(Integer::compareTo).orElse(1);

        Map<Integer, List<PlayerEntity>> rankingPorGrupo = new HashMap<>();

        for (int g : grupos.keySet()) {

            List<PlayerEntity> jugadores = grupos.get(g);

            List<PlayerEntity> ranking = jugadores.stream()
                    .sorted((a, b) -> {
                        int winsA = matchRepository.countWins(a.getLicenseNumber(), phaseCode);
                        int winsB = matchRepository.countWins(b.getLicenseNumber(), phaseCode);
                        return Integer.compare(winsB, winsA); // descendente
                    })
                    .toList();

            rankingPorGrupo.put(g, ranking);
        }

        for (int g = 1; g <= maxGroup; g++) {

            List<PlayerEntity> ranking = rankingPorGrupo.get(g);

            if (ranking == null || ranking.isEmpty()) continue;

            PlayerEntity primero = ranking.get(0);
            PlayerEntity ultimo = ranking.get(ranking.size() - 1);

            // Grupo 1: solo baja el último
            if (g == 1) {
                if (g + 1 <= maxGroup) {
                    ultimo.setGroupNo(g + 1);
                    playerRepository.save(ultimo);
                }
                continue;
            }

            // Último grupo: solo sube 1
            if (g == maxGroup) {
                primero.setGroupNo(g - 1);
                playerRepository.save(primero);
                continue;
            }

            // Grupos intermedios:
            // sube 1
            primero.setGroupNo(g - 1);
            playerRepository.save(primero);

            // baja 1
            ultimo.setGroupNo(g + 1);
            playerRepository.save(ultimo);
        }
    }

    /**
     * Calcula o crea la siguiente fase para el año actual usando los datos de los jugadores.
     * <p>
     * Regla:
     * - Si no existe ninguna fase para ese año → "YYYY-1"
     * - Si ya existe alguna fase para ese año → "YYYY-(max+1)"
     */
    @Override
    public String createOrAdvancePhaseForCurrentYear() {
        int currentYear = LocalDate.now().getYear();
        return createOrAdvancePhaseForYear(currentYear);
    }

    /**
     * Versión parametrizada (útil para tests) de la lógica de cálculo de siguiente phaseCode.
     */
    String createOrAdvancePhaseForYear(int year) {
        var optionalMax = playerRepository.findAll().stream()
                .map(PlayerEntity::getPhaseCode)
                .filter(code -> code != null && !code.isBlank())
                .map(PhaseCodeUtils::parse)
                .filter(pc -> pc.year() == year)
                .mapToInt(PhaseCodeUtils.PhaseComponents::phaseNumber)
                .max();

        if (optionalMax.isPresent()) {
            int nextPhaseNumber = optionalMax.getAsInt() + 1;
            return PhaseCodeUtils.buildPhaseCode(year, nextPhaseNumber);
        }

        // No había fases para ese año → primera fase
        return PhaseCodeUtils.buildPhaseCode(year, 1);
    }

}

