package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.config.JwtAuthFilter;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.exception.ConfirmMatchException;
import com.in4everyall.tennisclubmanager.tennisclub.exception.GlobalExceptionHandler;
import com.in4everyall.tennisclubmanager.tennisclub.exception.MatchAlreadyExistsException;
import com.in4everyall.tennisclubmanager.tennisclub.service.MatchService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuditConfig.class})
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnMatches_whenPlayerAndPhaseMonthValid() throws Exception {
        var match = buildMatchResponse(false, false);
        String phaseCode = "2025-1";
        given(matchService.findMyMatches("LIC-001", phaseCode)).willReturn(List.of(match));

        mockMvc.perform(get("/api/v1/matches/my")
                        .param("license", "LIC-001")
                        .param("phaseCode", "2025-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(match.id()))
                .andExpect(jsonPath("$[0].player1License").value("LIC-001"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnMyPhasesForPlayer() throws Exception {
        given(matchService.getPhaseCodesForPlayer("LIC-001")).willReturn(List.of("2025-1", "2025-2"));

        mockMvc.perform(get("/api/v1/matches/my-phases")
                        .param("license", "LIC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2025-1"))
                .andExpect(jsonPath("$[1]").value("2025-2"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnAllMatchesForPlayer() throws Exception {
        var match1 = buildMatchResponse(false, false);
        var match2 = buildMatchResponse(true, false);
        given(matchService.findAllMyMatches("LIC-001")).willReturn(List.of(match1, match2));

        mockMvc.perform(get("/api/v1/matches/my/all")
                        .param("license", "LIC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].player1License").value("LIC-001"))
                .andExpect(jsonPath("$[1].player1License").value("LIC-001"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnNotFound_whenMatchesMissing() throws Exception {
        String phaseCode = "2025-1";
        given(matchService.findMyMatches("LIC-001", phaseCode))
                .willThrow(new EntityNotFoundException("No hay partidos"));

        mockMvc.perform(get("/api/v1/matches/my")
                        .param("license", "LIC-001")
                        .param("phaseCode", "2025-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No hay partidos"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnExistsTrue_whenMatchAlreadyScheduled() throws Exception {
        String phaseCode = "2025-1";
        given(matchService.existsByPhaseAndPlayers(phaseCode, "LIC-001", "LIC-002")).willReturn(true);

        mockMvc.perform(get("/api/v1/matches/exists")
                        .param("phaseCode", "2025-1")
                        .param("p1", "LIC-001")
                        .param("p2", "LIC-002"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnBadRequest_whenExistsCheckFails() throws Exception {
        String phaseCode = "2025-1";
        given(matchService.existsByPhaseAndPlayers(phaseCode, "LIC-001", "LIC-001"))
                .willThrow(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Jugadores duplicados"));

        mockMvc.perform(get("/api/v1/matches/exists")
                        .param("phaseCode", "2025-1")
                        .param("p1", "LIC-001")
                        .param("p2", "LIC-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Jugadores duplicados"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldAddMatch_whenRequestValid() throws Exception {
        var matchResponse = buildMatchResponse(false, false);
        given(matchService.addMatch(any(MatchRequest.class))).willReturn(matchResponse);

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildMatchRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchResponse.id()))
                .andExpect(jsonPath("$.player2License").value("LIC-002"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnConflict_whenAddingDuplicatedMatch() throws Exception {
        given(matchService.addMatch(any(MatchRequest.class)))
                .willThrow(new MatchAlreadyExistsException("Partido ya existe"));

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildMatchRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Partido ya existe"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldConfirmMatch_whenPlayerAuthorized() throws Exception {
        UUID matchId = UUID.randomUUID();
        var confirmedMatch = buildMatchResponse(true, false);
        given(matchService.confirmMatch(eq(matchId), eq("LIC-002")))
                .willReturn(confirmedMatch);

        mockMvc.perform(patch("/api/v1/matches/{id}/confirm", matchId)
                        .param("confirmerLicense", "LIC-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnConflict_whenConfirmingInvalidMatch() throws Exception {
        UUID matchId = UUID.randomUUID();
        given(matchService.confirmMatch(eq(matchId), eq("LIC-002")))
                .willThrow(new ConfirmMatchException("No puedes confirmar"));

        mockMvc.perform(patch("/api/v1/matches/{id}/confirm", matchId)
                        .param("confirmerLicense", "LIC-002"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("No puedes confirmar"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldRejectMatch_whenPlayerRejects() throws Exception {
        UUID matchId = UUID.randomUUID();
        var rejectedMatch = buildMatchResponse(false, true);
        given(matchService.rejectMatch(eq(matchId), eq("LIC-002")))
                .willReturn(rejectedMatch);

        mockMvc.perform(patch("/api/v1/matches/{id}/reject", matchId)
                        .param("rejecterLicense", "LIC-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rejected").value(true));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldReturnForbidden_whenRejectNotAllowed() throws Exception {
        UUID matchId = UUID.randomUUID();
        given(matchService.rejectMatch(eq(matchId), eq("LIC-003")))
                .willThrow(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No autorizado"));

        mockMvc.perform(patch("/api/v1/matches/{id}/reject", matchId)
                        .param("rejecterLicense", "LIC-003"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No autorizado"));
    }

    private MatchRequest buildMatchRequest() {
        return new MatchRequest(
                "2025-1",
                Instant.parse("2025-01-10T10:00:00Z"),
                Instant.parse("2025-01-11T10:00:00Z"),
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

    private MatchResponse buildMatchResponse(boolean confirmed, boolean rejected) {
        String status;
        if (rejected) {
            status = "REJECTED";
        } else if (confirmed) {
            status = "CONFIRMED";
        } else {
            status = "PENDING";
        }
        return new MatchResponse(
                UUID.randomUUID().toString(),
                1,
                "2025-1",
                Instant.parse("2025-01-10T10:00:00Z"),
                Instant.parse("2025-01-10T12:00:00Z"),
                "LIC-001",
                "Player One",
                "LIC-002",
                "Player Two",
                "LIC-001",
                "Player One",
                (short) 6,
                (short) 4,
                (short) 6,
                (short) 3,
                null,
                null,
                "LIC-001",
                confirmed,
                rejected,
                false,
                status
        );
    }
}

