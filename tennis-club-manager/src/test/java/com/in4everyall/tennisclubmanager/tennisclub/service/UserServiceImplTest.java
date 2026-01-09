package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.PlayerMapper;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.UserMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.validator.SignUpValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PlayerMapper playerMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SignUpValidator signUpValidator;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void shouldSignUpUserWhenDataValid() {
        var request = request();
        var entity = UserEntity.builder()
                .licenseNumber("LIC-123")
                .email("ana@club.com")
                .role(Role.PLAYER)
                .passwordHash("raw")
                .build();
        var response = new UserResponse("LIC-123", "Ana", "García", LocalDate.now(), "ana@club.com", "PLAYER", "+34000111222", null);

        given(userRepository.existsByLicenseNumber("LIC-123")).willReturn(false);
        given(userRepository.existsByEmail("ana@club.com")).willReturn(false);
        given(userMapper.toEntity(request)).willReturn(entity);
        given(passwordEncoder.encode("password1")).willReturn("encoded");
        given(playerMapper.toEntity(request)).willReturn(PlayerEntity.builder().licenseNumber("LIC-123").build());
        given(userMapper.toResponse(entity)).willReturn(response);

        UserResponse result = service.signUp(request);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getPasswordHash()).isEqualTo("encoded");
        verify(userRepository).save(entity);
        verify(playerRepository).save(any(PlayerEntity.class));
        verify(signUpValidator).validateSignUpForm(request);
    }

    @Test
    void shouldThrowWhenLicenseAlreadyExists() {
        var request = request();
        given(userRepository.existsByLicenseNumber("LIC-123")).willReturn(true);

        assertThatThrownBy(() -> service.signUp(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void shouldLoginWhenCredentialsMatch() {
        var request = new LoginRequest("ana@club.com", "plain");
        var entity = UserEntity.builder()
                .email("ana@club.com")
                .passwordHash("hashed")
                .licenseNumber("LIC-123")
                .role(Role.PLAYER)
                .build();
        var response = new UserResponse("LIC-123", "Ana", "García", null, "ana@club.com", "PLAYER", null, "jwt");

        given(userRepository.findByEmail("ana@club.com")).willReturn(Optional.of(entity));
        given(passwordEncoder.matches("plain", "hashed")).willReturn(true);
        given(jwtService.generateToken("ana@club.com")).willReturn("jwt");
        given(userMapper.toResponse(entity, "jwt")).willReturn(response);

        UserResponse result = service.login(request);

        assertThat(result.token()).isEqualTo("jwt");
        verify(jwtService).generateToken("ana@club.com");
    }

    private SignUpRequest request() {
        return new SignUpRequest(
                "LIC-123",
                "Ana",
                "García",
                "1990-01-01",
                "top",
                "slice",
                "ana@club.com",
                "password1",
                "password1",
                "PLAYER",
                "+34000111222"
        );
    }
}

