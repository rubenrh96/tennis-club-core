package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.config.JwtAuthFilter;
import com.in4everyall.tennisclubmanager.tennisclub.dto.AdminMatchesSummary;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerUpdateRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.StandingRow;
import com.in4everyall.tennisclubmanager.tennisclub.exception.GlobalExceptionHandler;
import com.in4everyall.tennisclubmanager.tennisclub.service.AdminService;
import com.in4everyall.tennisclubmanager.tennisclub.service.MatchService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuditConfig.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private MatchService matchService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPlayers_whenAdminRequestsList() throws Exception {
        given(playerService.getAllPlayers()).willReturn(List.of(samplePlayer()));

        mockMvc.perform(get("/api/v1/admin/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].license").value("LIC-001"))
                .andExpect(jsonPath("$[0].groupNo").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnServerError_whenListingPlayersFails() throws Exception {
        given(playerService.getAllPlayers()).willThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/v1/admin/players"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnGroupStandings_whenAdminRequestsData() throws Exception {
        String phaseCode = "2025-2";
        given(rankingService.getStandingsForGroup(1, phaseCode)).willReturn(List.of(sampleStanding()));

        mockMvc.perform(get("/api/v1/admin/groups/{groupNo}/standings", 1)
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupNo").value(1))
                .andExpect(jsonPath("$[0].position").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFound_whenGroupStandingsUnavailable() throws Exception {
        String phaseCode = "2025-2";
        given(rankingService.getStandingsForGroup(1, phaseCode))
                .willThrow(new EntityNotFoundException("Grupo no existe"));

        mockMvc.perform(get("/api/v1/admin/groups/{groupNo}/standings", 1)
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Grupo no existe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdatePlayer_whenPayloadValid() throws Exception {
        var update = new PlayerUpdateRequest("Jane", "Smith", 2, "600999999");
        var updated = new PlayerGroupItemResponse("LIC-002", "Jane Smith", 2, "2025-02", "600999999");
        given(playerService.updatePlayer(eq("LIC-002"), any(PlayerUpdateRequest.class))).willReturn(updated);

        mockMvc.perform(put("/api/v1/admin/players/{license}", "LIC-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.license").value("LIC-002"))
                .andExpect(jsonPath("$.groupNo").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFound_whenUpdatingUnknownPlayer() throws Exception {
        given(playerService.updatePlayer(eq("UNKNOWN"), any(PlayerUpdateRequest.class)))
                .willThrow(new EntityNotFoundException("Jugador no encontrado"));

        var update = new PlayerUpdateRequest("Jane", "Smith", 2, "600999999");

        mockMvc.perform(put("/api/v1/admin/players/{license}", "UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Jugador no encontrado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeletePlayer_whenLicenseExists() throws Exception {
        willDoNothing().given(playerService).deletePlayer("LIC-002");

        mockMvc.perform(delete("/api/v1/admin/players/{license}", "LIC-002"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFound_whenDeletingUnknownPlayer() throws Exception {
        willThrow(new EntityNotFoundException("Jugador no encontrado"))
                .given(playerService).deletePlayer("UNKNOWN");

        mockMvc.perform(delete("/api/v1/admin/players/{license}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Jugador no encontrado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnMatchesSummary_whenPhaseMonthProvided() throws Exception {
        var summary = new AdminMatchesSummary(
                "2025-2",
                10,
                4,
                List.of(sampleMatchResponse())
        );
        String phaseCode = "2025-2";
        given(adminService.getMatchesByPhase(phaseCode)).willReturn(summary);

        mockMvc.perform(get("/api/v1/admin/matches")
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phaseCode").value("2025-2"))
                .andExpect(jsonPath("$.existingMatches").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAllPhasesForAdmin() throws Exception {
        given(matchService.getAllPhaseCodesForAdmin()).willReturn(List.of("2024-4", "2025-1", "2025-2"));

        mockMvc.perform(get("/api/v1/admin/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2024-4"))
                .andExpect(jsonPath("$[1]").value("2025-1"))
                .andExpect(jsonPath("$[2]").value("2025-2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_whenMatchesSummaryFails() throws Exception {
        String phaseCode = "2025-2";
        given(adminService.getMatchesByPhase(phaseCode))
                .willThrow(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Mes inválido"));

        mockMvc.perform(get("/api/v1/admin/matches")
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Mes inválido"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddMatchAdmin_whenPayloadValid() throws Exception {
        var matchResponse = sampleMatchResponse();
        given(matchService.adminAddMatch(eq("ADMIN-1"), any(MatchRequest.class))).willReturn(matchResponse);

        mockMvc.perform(post("/api/v1/admin/matches")
                        .param("adminLicense", "ADMIN-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMatchRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchResponse.id()))
                .andExpect(jsonPath("$.groupNo").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForbidden_whenAdminAddMatchUnauthorized() throws Exception {
        given(matchService.adminAddMatch(eq("ADMIN-2"), any(MatchRequest.class)))
                .willThrow(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Sin permisos"));

        mockMvc.perform(post("/api/v1/admin/matches")
                        .param("adminLicense", "ADMIN-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMatchRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Sin permisos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldConfirmAllMatches_whenPhaseMonthValid() throws Exception {
        String phaseCode = "2025-2";
        willDoNothing().given(adminService).confirmAll(phaseCode);

        mockMvc.perform(post("/api/v1/admin/matches/confirm-all")
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_whenConfirmAllFails() throws Exception {
        String phaseCode = "2025-2";
        willThrow(new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "No se puede cerrar el mes"))
                .given(adminService).confirmAll(phaseCode);

        mockMvc.perform(post("/api/v1/admin/matches/confirm-all")
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No se puede cerrar el mes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldClosePhase_whenAdminRequestsIt() throws Exception {
        String phaseCode = "2025-2";
        willDoNothing().given(adminService).closePhase(phaseCode);

        mockMvc.perform(post("/api/v1/admin/phase/close")
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Fase cerrada y grupos actualizados"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_whenClosePhaseFails() throws Exception {
        String phaseCode = "2025-2";
        willThrow(new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Fase ya cerrada"))
                .given(adminService).closePhase(phaseCode);

        mockMvc.perform(post("/api/v1/admin/phase/close")
                        .param("phaseCode", "2025-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Fase ya cerrada"));
    }

    private PlayerGroupItemResponse samplePlayer() {
        return new PlayerGroupItemResponse("LIC-001", "John Smith", 1, "2025-01", "600111111");
    }

    private StandingRow sampleStanding() {
        return new StandingRow(
                "LIC-001",
                "John Smith",
                1,
                4,
                4,
                0,
                12,
                8,
                2,
                48,
                20,
                1
        );
    }

    private MatchRequest sampleMatchRequest() {
        return new MatchRequest(
                "2025-2",
                Instant.parse("2025-02-01T10:00:00Z"),
                Instant.parse("2025-02-02T10:00:00Z"),
                "LIC-001",
                "p1@club.com",
                "LIC-002",
                "p2@club.com",
                "LIC-001",
                "p1@club.com",
                (short) 6,
                (short) 4,
                (short) 6,
                (short) 3,
                null,
                null,
                "LIC-001",
                true
        );
    }

    private MatchResponse sampleMatchResponse() {
        return new MatchResponse(
                "match-1",
                1,
                "2025-2",
                Instant.parse("2025-02-01T10:00:00Z"),
                Instant.parse("2025-02-02T12:00:00Z"),
                "LIC-001",
                "Player One",
                "LIC-002",
                "Player Two",
                "LIC-001",
                "Player One",
                (short) 6,
                (short) 4,
                (short) 7,
                (short) 6,
                null,
                null,
                "LIC-001",
                true,
                false,
                false,
                "CONFIRMED"
        );
    }
}

