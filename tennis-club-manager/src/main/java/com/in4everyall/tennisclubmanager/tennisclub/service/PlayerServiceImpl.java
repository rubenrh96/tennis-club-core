package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerUpdateRequest;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.exception.DeletePlayerException;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.PlayerMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.MatchRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    private final MatchRepository matchRepository;

    private final UserRepository userRepository;

    private final PlayerMapper playerMapper;

    public List<PlayerGroupItemResponse> getAllPlayers(){
        List<PlayerEntity> playerEntities  = playerRepository.findAll();
        if(playerEntities.isEmpty())
        {
            throw new IllegalArgumentException("No se encontraron jugadores");
        }
        return playerEntities.stream()
            .sorted(Comparator.comparing(
                    PlayerEntity::getGroupNo,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .map(player -> new PlayerGroupItemResponse(
                    player.getLicenseNumber(),
                    player.getUser().getFirstName() + " " + player.getUser().getLastName(),
                    player.getGroupNo(),
                    player.getPhaseCode(),
                    player.getUser().getPhone()
            ))
            .toList();
    }

    @Override
    public PlayerEntity findByLicense(String license) {
        if (license != null) {
            return playerRepository.findByLicenseNumber(license)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró jugador con license " + license));
        } else {
            throw new IllegalArgumentException("Debe especificar license del jugador");
        }
    }

    @Override
    public PlayerEntity findOptionalByLicense(String license) {
        if (license == null) return null;
        return findByLicense(license);
    }

    @Override
    public PlayerGroupItemResponse updatePlayer(String license, PlayerUpdateRequest request) {
        PlayerEntity player = playerRepository.findByLicenseNumber(license)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado"));
        if (request.groupNo() != null) {
            player.setGroupNo(request.groupNo());
        }
        if (request.phone() != null) {
            player.getUser().setPhone(request.phone());
        }

        UserEntity user = player.getUser();
        if (user != null) {
            if (request.firstName() != null) {
                user.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                user.setLastName(request.lastName());
            }
            userRepository.save(user);
        }

        playerRepository.save(player);

        return playerMapper.toGroupItemResponse(player);
    }

    @Transactional
    @Override
    public void deletePlayer(String license) {
        PlayerEntity player = playerRepository.findByLicenseNumber(license)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado"));

        boolean hasMatches = matchRepository.existsByPlayer1_LicenseNumberOrPlayer2_LicenseNumber(license, license);
        if (hasMatches) {
            throw new DeletePlayerException("No se puede eliminar el jugador porque tiene partidos asociados.");
        }

        UserEntity user = player.getUser();
        if (user != null) {
            userRepository.save(user);
        }
        player.setUser(null);

        playerRepository.deleteById(license);
    }

    public List<PlayerGroupItemResponse> getMyGroupPlayers(String myLicense) {
        PlayerEntity me = playerRepository.findById(myLicense)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado"));

        List<PlayerEntity> groupPlayers = playerRepository.findByGroupNoAndPhaseCode(
                me.getGroupNo(),
                me.getPhaseCode()
        );

        return groupPlayers.stream()
                .filter(p -> !p.getLicenseNumber().equals(myLicense))
                .map(p -> {
                    assert p.getUser() != null;
                    return new PlayerGroupItemResponse(
                            p.getLicenseNumber(),
                            p.getUser().getFirstName() + " " + p.getUser().getLastName(),
                            p.getGroupNo(),
                            p.getPhaseCode(),
                            p.getUser().getPhone()
                    );
                })
                .toList();
    }
}
