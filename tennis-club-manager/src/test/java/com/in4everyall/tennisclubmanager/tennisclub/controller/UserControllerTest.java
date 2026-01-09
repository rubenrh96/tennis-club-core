package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.config.JwtAuthFilter;
import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.exception.GlobalExceptionHandler;
import com.in4everyall.tennisclubmanager.tennisclub.service.UserService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuditConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    void shouldSignUpUser_whenPayloadIsValid() throws Exception {
        var response = buildUserResponse();

        given(userService.signUp(any(SignUpRequest.class))).willReturn(response);

        var payload = new SignUpRequest(
                "LIC-001", "John", "Doe", "1990-05-20",
                "TOP", "BACK", "john@club.com", "secret",
                "secret", "PLAYER", "600123123"
        );

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenseNumber").value("LIC-001"))
                .andExpect(jsonPath("$.email").value("john@club.com"))
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    void shouldReturnConflict_whenSignUpEmailAlreadyUsed() throws Exception {
        given(userService.signUp(any(SignUpRequest.class)))
                .willThrow(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.CONFLICT, "Email ya usado"));

        var payload = new SignUpRequest(
                "LIC-001", "John", "Doe", "1990-05-20",
                "TOP", "BACK", "john@club.com", "secret",
                "secret", "PLAYER", "600123123"
        );

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email ya usado"));
    }

    @Test
    void shouldLoginUser_whenCredentialsAreValid() throws Exception {
        var response = buildUserResponse();
        given(userService.login(any(LoginRequest.class))).willReturn(response);

        var payload = new LoginRequest("john@club.com", "secret");

        mockMvc.perform(post("/api/v1/users/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldReturnUnauthorized_whenLoginFails() throws Exception {
        given(userService.login(any(LoginRequest.class)))
                .willThrow(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        var payload = new LoginRequest("john@club.com", "bad");

        mockMvc.perform(post("/api/v1/users/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void shouldReturnUser_whenLicenseExists() throws Exception {
        var response = buildUserResponse();
        given(userService.findByLicenseNumber("LIC-001")).willReturn(response);

        mockMvc.perform(get("/api/v1/users/{licenseNumber}", "LIC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenseNumber").value("LIC-001"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldReturnNotFound_whenLicenseDoesNotExist() throws Exception {
        given(userService.findByLicenseNumber("UNKNOWN"))
                .willThrow(new EntityNotFoundException("Jugador no encontrado"));

        mockMvc.perform(get("/api/v1/users/{licenseNumber}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Jugador no encontrado"))
                .andExpect(jsonPath("$.details").isArray());
    }

    private UserResponse buildUserResponse() {
        return new UserResponse(
                "LIC-001",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 20),
                "john@club.com",
                "PLAYER",
                "600123123",
                "jwt-token"
        );
    }
}

