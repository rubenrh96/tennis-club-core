package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.PlayerMapper;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.UserMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.validator.SignUpValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SignUpValidator signUpValidator;

    @Override
    public UserResponse signUp(SignUpRequest req) {
        signUpValidator.validateSignUpForm(req);
        if (userRepository.existsByLicenseNumber(req.licenseNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "License already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        UserEntity userEntity = userMapper.toEntity(req);
        userEntity.setPasswordHash(passwordEncoder.encode(req.passwordHash()));
        userRepository.save(userEntity);
        playerRepository.save(playerMapper.toEntity(req));
        return userMapper.toResponse(userEntity);
    }

    @Override
    public UserResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail());


        return userMapper.toResponse(user,token);
    }

    public UserResponse findByLicenseNumber(String licenseNumber) {
        UserEntity user = userRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }
}
