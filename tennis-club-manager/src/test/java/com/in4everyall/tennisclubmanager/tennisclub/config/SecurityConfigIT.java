package com.in4everyall.tennisclubmanager.tennisclub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.service.AdminService;
import com.in4everyall.tennisclubmanager.tennisclub.service.MatchService;
import com.in4everyall.tennisclubmanager.tennisclub.service.PlayerService;
import com.in4everyall.tennisclubmanager.tennisclub.service.RankingService;
import com.in4everyall.tennisclubmanager.tennisclub.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:securitydb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.type.preferred_enum_type=string",
        "spring.flyway.enabled=false"
})
class SecurityConfigIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserService userService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private MatchService matchService;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void shouldAllowAnonymousAccessToPublicAuthEndpoints() throws Exception {
        when(userService.signUp(any())).thenReturn(dummyUserResponse());
        when(userService.login(any())).thenReturn(dummyUserResponse());

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummySignUpRequest())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/users/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ana@test.com", "pwd"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/players/my-group")
                        .param("license", "LIC-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldAllowPlayerEndpointForPlayerRole() throws Exception {
        when(playerService.getMyGroupPlayers("LIC-1")).thenReturn(Collections.<PlayerGroupItemResponse>emptyList());

        mockMvc.perform(get("/api/v1/players/my-group")
                        .param("license", "LIC-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void shouldForbidAdminEndpointForPlayerRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/players"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminEndpointForAdminRole() throws Exception {
        when(playerService.getAllPlayers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/players"))
                .andExpect(status().isOk());
    }

    private SignUpRequest dummySignUpRequest() {
        return new SignUpRequest(
                "LIC-1",
                "Ana",
                "Lopez",
                "1990-01-01",
                "TOP",
                "SLICE",
                "ana@test.com",
                "pwd",
                "pwd",
                Role.PLAYER.name(),
                "+34999999999"
        );
    }

    private UserResponse dummyUserResponse() {
        return new UserResponse(
                "LIC-1",
                "Ana",
                "Lopez",
                LocalDate.parse("1990-01-01"),
                "ana@test.com",
                Role.PLAYER.name(),
                "+34999999999",
                "token"
        );
    }
}

