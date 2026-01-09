package com.in4everyall.tennisclubmanager.tennisclub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",           // Front web
                "http://192.168.1.175:3000",       // Front web desde red local
                "exp://192.168.1.175:8081",        // Expo Go Casa
                "http://192.168.1.175:8081",
                "exp://192.168.1.138:8081",        // Expo Go Casa Raquel
                "http://192.168.1.138:8081",
                "exp://192.168.8.109:8081",        // Expo Go Pueblo
                "http://192.168.8.109:8081"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Puedes limitar a "/api/v1/**" si quieres
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())  // usa el bean de arriba
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints públicos
                        .requestMatchers(
                                "/api/v1/users/log-in",
                                "/api/v1/users/sign-up",
                                "/api/v1/users/forgot-password",
                                "/api/v1/users/reset-password"
                        ).permitAll()
                        .requestMatchers("/api/v1/players/**")
                        .hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/v1/player/**")
                        .hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/v1/matches/**")
                        .hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
