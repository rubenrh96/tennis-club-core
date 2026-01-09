package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerUpdateRequest;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import org.hibernate.validator.constraints.UUID;

import java.util.List;

public interface PlayerService {
    PlayerEntity findByLicense(String license);

    PlayerEntity findOptionalByLicense(String license);

    List<PlayerGroupItemResponse> getMyGroupPlayers(String myLicense);

    List<PlayerGroupItemResponse> getAllPlayers();

    PlayerGroupItemResponse updatePlayer(String license, PlayerUpdateRequest request);

    void deletePlayer(String license);
}
