package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.type.preferred_enum_type=string"
})
@Import(AuditConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByEmailAndLicense() {
        UserEntity user = UserEntity.builder()
                .email("user@test.com")
                .licenseNumber("LIC-1")
                .firstName("User")
                .lastName("Test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .passwordHash("pwd")
                .role(Role.PLAYER)
                .phone("+34000000000")
                .build();
        userRepository.save(user);

        assertThat(userRepository.findByEmail("user@test.com")).isPresent();
        assertThat(userRepository.findByLicenseNumber("LIC-1")).isPresent();
        assertThat(userRepository.existsByEmail("user@test.com")).isTrue();
        assertThat(userRepository.existsByLicenseNumber("LIC-1")).isTrue();
    }
}

