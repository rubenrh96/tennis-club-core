package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@RequestBody SignUpRequest req) {
        return ResponseEntity.ok(userService.signUp(req));
    }

    @PostMapping("/log-in")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    @GetMapping("/{licenseNumber}")
    public ResponseEntity<UserResponse> getUserByLicense(@PathVariable String licenseNumber) {
        UserResponse response = userService.findByLicenseNumber(licenseNumber);
        return ResponseEntity.ok(response);
    }
}
