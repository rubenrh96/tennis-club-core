package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.config.JwtAuthFilter;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.StandingRow;
import com.in4everyall.tennisclubmanager.tennisclub.exception.GlobalExceptionHandler;
import com.in4everyall.tennisclubmanager.tennisclub.service.PlayerService;
import com.in4everyall.tennisclubmanager.tennisclub.service.RankingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuditConfig.class})
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnMyGroup_whenLicenseExists() throws Exception {
        var response = new PlayerGroupItemResponse("LIC-001", "John Smith", 1, "2025-01", "600111111");
        given(playerService.getMyGroupPlayers("LIC-001")).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/players/my-group")
                        .param("license", "LIC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].license").value("LIC-001"))
                .andExpect(jsonPath("$[0].groupNo").value(1));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnNotFound_whenLicenseUnknownOnGroup() throws Exception {
        given(playerService.getMyGroupPlayers("UNKNOWN"))
                .willThrow(new EntityNotFoundException("Jugador no existe"));

        mockMvc.perform(get("/api/v1/players/my-group")
                        .param("license", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Jugador no existe"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnStandings_whenPhaseMonthValid() throws Exception {
        var standing = new StandingRow(
                "LIC-001", "John Smith", 1,
                3, 3, 0, 9, 6, 0, 36, 12, 1
        );
        String phaseCode = "2025-1";
        given(rankingService.getStandingsForPlayerDashboard("LIC-001", phaseCode))
                .willReturn(List.of(standing));

        mockMvc.perform(get("/api/v1/players/{license}/standings", "LIC-001")
                        .param("phaseCode", "2025-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licenseNumber").value("LIC-001"))
                .andExpect(jsonPath("$[0].position").value(1));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnNotFound_whenStandingsUnavailable() throws Exception {
        String phaseCode = "2025-1";
        given(rankingService.getStandingsForPlayerDashboard("LIC-001", phaseCode))
                .willThrow(new EntityNotFoundException("No hay clasificaciones"));

        mockMvc.perform(get("/api/v1/players/{license}/standings", "LIC-001")
                        .param("phaseCode", "2025-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No hay clasificaciones"));
    }
}

