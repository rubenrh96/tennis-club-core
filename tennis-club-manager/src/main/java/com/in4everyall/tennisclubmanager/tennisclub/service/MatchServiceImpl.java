package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.exception.AddMatchException;
import com.in4everyall.tennisclubmanager.tennisclub.exception.ConfirmMatchException;
import com.in4everyall.tennisclubmanager.tennisclub.exception.MatchAlreadyExistsException;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.MatchMapper;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.validator.SetValidator;
import com.in4everyall.tennisclubmanager.tennisclub.validator.WinnerValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService{

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchMapper matchMapper;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SetValidator setValidator;

    @Autowired
    private WinnerValidator winnerValidator;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<MatchResponse> findMyMatches(String license, String phaseCode) {
        var matches = matchRepository.findByPlayerAndPhaseCode(license, phaseCode);
        return matches.stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    @Override
    public MatchResponse addMatch(MatchRequest matchRequest) {
        if (matchRequest.player1License() == null || matchRequest.player2License() == null) {
            throw new AddMatchException("Debe especificar el jugador");
        }
        if (matchRequest.player1License().equals(matchRequest.player2License())) {
            throw new AddMatchException("Un jugador no puede jugar contra sí mismo");
        }
        if (matchRepository.existsByPhaseCodeAndPlayers(matchRequest.phaseCode(), matchRequest.player1License(), matchRequest.player2License())) {
            throw new MatchAlreadyExistsException("Ya hay un resultado PENDIENTE entre vosotros dos.");
        }

        setValidator.validateSets(matchRequest.set1P1(), matchRequest.set1P2(), matchRequest.set2P1(), matchRequest.set2P2(), matchRequest.set3P1(), matchRequest.set3P2());
        winnerValidator.validateWinner(matchRequest);
        MatchEntity entity = matchMapper.toEntity(matchRequest);
        PlayerEntity player1 = playerService.findByLicense(matchRequest.player1License());
        PlayerEntity player2 = playerService.findByLicense(matchRequest.player2License());
        PlayerEntity winner = playerService.findOptionalByLicense(matchRequest.winnerLicense());
        // La fase funcional del partido siempre se toma de la fase actual del jugador,
        // ignorando lo que pueda venir del cliente.
        entity.setPhaseCode(player1.getPhaseCode());
        // Grupo en el momento del partido: tomamos el del jugador1 como referencia.
        entity.setGroupNoAtMatch(player1.getGroupNo());
        entity.setPlayer1(player1);
        entity.setPlayer2(player2);
        entity.setWinner(winner);
        entity.setSubmittedByLicense(player1.getLicenseNumber());
        entity.setConfirmed(false);
        entity.setCancelled(false);
        MatchEntity saved = matchRepository.save(entity);
        return matchMapper.toResponse(saved);
    }

    @Override
    public MatchResponse adminAddMatch(String adminLicense, MatchRequest matchRequest) {

        // --- Validaciones básicas iguales que en addMatch del player ---
        if (matchRequest.player1License() == null || matchRequest.player2License() == null) {
            throw new AddMatchException("Debe especificar los dos jugadores");
        }
        if (matchRequest.player1License().equals(matchRequest.player2License())) {
            throw new AddMatchException("Un jugador no puede jugar contra sí mismo");
        }

        // Validar sets y ganador
        setValidator.validateSets(
                matchRequest.set1P1(), matchRequest.set1P2(),
                matchRequest.set2P1(), matchRequest.set2P2(),
                matchRequest.set3P1(), matchRequest.set3P2()
        );
        winnerValidator.validateWinner(matchRequest);

        // --- Buscar el partido existente entre esos jugadores en ese mes ---
        MatchEntity existing = matchRepository
                .findByPhaseCodeAndPlayers(
                        matchRequest.phaseCode(),           // phaseCode enviado desde el front
                        matchRequest.player1License(),
                        matchRequest.player2License()
                )
                .orElseThrow(() -> new AddMatchException(
                        "No existe un partido previo entre esos jugadores en este mes para corregir."
                ));

        // Permitimos corregir o editar si estaba RECHAZADO o CONFIRMADO
//        if (!existing.isRejected()) {
//            throw new AddMatchException("Solo se pueden corregir partidos en estado RECHAZADO.");
//        }

        // --- Cargar jugadores y ganador ---
        PlayerEntity player1 = playerService.findByLicense(matchRequest.player1License());
        PlayerEntity player2 = playerService.findByLicense(matchRequest.player2License());
        PlayerEntity winner = playerService.findOptionalByLicense(matchRequest.winnerLicense());

        // --- Actualizar datos del partido existente ---
        existing.setPlayer1(player1);
        existing.setPlayer2(player2);
        existing.setWinner(winner);

        existing.setSet1P1(matchRequest.set1P1());
        existing.setSet1P2(matchRequest.set1P2());
        existing.setSet2P1(matchRequest.set2P1());
        existing.setSet2P2(matchRequest.set2P2());
        existing.setSet3P1(matchRequest.set3P1());
        existing.setSet3P2(matchRequest.set3P2());

        // Fase: usamos la del propio jugador (igual que en addMatch)
        existing.setPhaseCode(player1.getPhaseCode());
        // Grupo en el momento del partido (corregido): tomamos el del jugador1 como referencia.
        existing.setGroupNoAtMatch(player1.getGroupNo());

        // Lo firma el admin y queda confirmado
        //existing.setSubmittedByLicense(adminLicense);
        existing.setConfirmed(true);
        existing.setRejected(false);
        existing.setCancelled(false);

        // Si quieres, también puedes actualizar fechas
        existing.setScheduledAt(Instant.now());
        existing.setPlayedAt(Instant.now());

        MatchEntity saved = matchRepository.save(existing);
        return matchMapper.toResponse(saved);
    }

    @Transactional
    public MatchResponse confirmMatch(UUID matchId, String confirmerLicense) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ConfirmMatchException("Partido no encontrado"));

        if (match.isConfirmed()) {
            throw new ConfirmMatchException("El resultado ya ha sido confirmado");
        }
        if (Objects.equals(match.getSubmittedByLicense(), confirmerLicense)) {
            throw new ConfirmMatchException("El jugador que envió el resultado no puede confirmarlo");
        }
        match.setConfirmed(true);
        match.setUpdatedBy(confirmerLicense);
        match.setUpdatedDate(Instant.now());
        matchRepository.save(match);
        return matchMapper.toResponse(match);
    }

    public boolean existsByPhaseAndPlayers(String phaseCode, String p1, String p2) {
        return matchRepository.existsByPhaseCodeAndPlayers(phaseCode, p1, p2);
    }
    
    @Transactional
    public MatchResponse rejectMatch(UUID matchId, String rejecterLicense) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ConfirmMatchException("Partido no encontrado"));

        if (match.isConfirmed()) {
            throw new ConfirmMatchException("El resultado ya ha sido confirmado");
        }
        if (Objects.equals(match.getSubmittedByLicense(), rejecterLicense)) {
            throw new ConfirmMatchException("Error en el rechazo");
        }
        match.setRejected(true);
        match.setUpdatedBy(rejecterLicense);
        match.setUpdatedDate(Instant.now());
        matchRepository.save(match);
        return matchMapper.toResponse(match);
    }

    @Override
    public List<String> getAllPhaseCodesForAdmin() {
        return matchRepository.findAllPhaseCodes();
    }

    @Override
    public List<String> getPhaseCodesForPlayer(String license) {
        return matchRepository.findPhaseCodesByPlayer(license);
    }

    @Override
    public List<MatchResponse> findAllMyMatches(String license) {
        return matchRepository.findAllByPlayer(license).stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MatchResponse cancelMatch(UUID matchId, String adminLicense) {
        // 1. Verificar que el usuario es admin
        UserEntity admin = userRepository.findByLicenseNumber(adminLicense)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo los administradores pueden cancelar partidos");
        }

        // 2. Obtener el partido
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Partido no encontrado"));

        // 3. Validar que no está confirmado
        if (match.isConfirmed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No se puede cancelar un partido confirmado");
        }

        // 4. Validar que no está ya cancelado
        if (Boolean.TRUE.equals(match.getCancelled())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El partido ya está cancelado");
        }

        // 5. Marcar como cancelado
        match.setCancelled(true);
        match.setUpdatedBy(adminLicense);
        match.setUpdatedDate(Instant.now());
        match = matchRepository.save(match);

        // 6. Retornar respuesta
        return matchMapper.toResponse(match);
    }

}
