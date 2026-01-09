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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerServiceImpl service;

    @Test
    void shouldReturnSortedPlayers() {
        var playerA = player("LIC-1", 2, "Ana", "López");
        var playerB = player("LIC-2", 1, "Beto", "Ruiz");
        given(playerRepository.findAll()).willReturn(List.of(playerA, playerB));

        List<PlayerGroupItemResponse> result = service.getAllPlayers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).groupNo()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenDeletingPlayerWithMatches() {
        given(playerRepository.findByLicenseNumber("LIC-1")).willReturn(Optional.of(player("LIC-1", 1, "Ana", "López")));
        given(matchRepository.existsByPlayer1_LicenseNumberOrPlayer2_LicenseNumber("LIC-1", "LIC-1")).willReturn(true);

        assertThatThrownBy(() -> service.deletePlayer("LIC-1"))
                .isInstanceOf(DeletePlayerException.class)
                .hasMessageContaining("partidos asociados");
    }

    @Test
    void shouldUpdatePlayerNamesAndGroup() {
        var player = player("LIC-3", 1, "Ana", "Vieja");
        given(playerRepository.findByLicenseNumber("LIC-3")).willReturn(Optional.of(player));
        given(playerMapper.toGroupItemResponse(player)).willReturn(new PlayerGroupItemResponse("LIC-3", "Nueva", 2, "2025-03", "+34999"));

        var request = new PlayerUpdateRequest("Nueva", "Apellido", 2, "+34999");
        PlayerGroupItemResponse response = service.updatePlayer("LIC-3", request);

        assertThat(player.getGroupNo()).isEqualTo(2);
        assertThat(player.getUser().getFirstName()).isEqualTo("Nueva");
        verify(userRepository).save(player.getUser());
        verify(playerRepository).save(player);
        assertThat(response.groupNo()).isEqualTo(2);
    }

    @Test
    void shouldFailUpdateWhenPlayerMissing() {
        given(playerRepository.findByLicenseNumber("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePlayer("UNKNOWN", new PlayerUpdateRequest(null, null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Jugador no encontrado");
    }

    private PlayerEntity player(String license, int group, String first, String last) {
        return PlayerEntity.builder()
                .licenseNumber(license)
                .groupNo(group)
                .phaseCode("2025-1")
                .user(UserEntity.builder()
                        .licenseNumber(license)
                        .firstName(first)
                        .lastName(last)
                        .phone("+34000000000")
                        .email(first.toLowerCase() + "@club.com")
                        .build())
                .build();
    }
}

