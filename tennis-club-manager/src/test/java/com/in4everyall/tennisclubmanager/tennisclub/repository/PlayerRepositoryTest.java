package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.type.preferred_enum_type=string"
})
@Import(AuditConfig.class)
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByLicenseAndPhaseMonth() {
        persistPlayer("LIC-1", 1, "2025-1");

        assertThat(playerRepository.findByLicenseNumberAndPhaseCode("LIC-1", "2025-1"))
                .isPresent();
        assertThat(playerRepository.findByLicenseNumberAndPhaseCode("LIC-1", "2025-2"))
                .isNotPresent();
    }

    @Test
    void shouldFindByGroupAndPhaseMonth() {
        persistPlayer("LIC-1", 1, "2025-3");
        persistPlayer("LIC-2", 1, "2025-3");
        persistPlayer("LIC-3", 2, "2025-3");

        List<PlayerEntity> group1 = playerRepository.findByGroupNoAndPhaseCode(1, "2025-3");

        assertThat(group1).extracting(PlayerEntity::getLicenseNumber)
                .containsExactlyInAnyOrder("LIC-1", "LIC-2");
    }

    @Test
    void shouldFindByPhaseMonth() {
        persistPlayer("LIC-1", 1, "2025-4");
        persistPlayer("LIC-2", 1, "2025-4");

        List<PlayerEntity> players = playerRepository.findByPhaseCode("2025-4");

        assertThat(players).hasSize(2);
    }

    private PlayerEntity persistPlayer(String license, int group, String phaseCode) {
        UserEntity user = UserEntity.builder()
                .email(license.toLowerCase() + "@club.test")
                .licenseNumber(license)
                .firstName("Name" + license)
                .lastName("Last" + license)
                .birthDate(LocalDate.of(1990, 1, 1))
                .passwordHash("pwd")
                .role(Role.PLAYER)
                .phone("+34111111111")
                .build();
        userRepository.save(user);

        PlayerEntity player = PlayerEntity.builder()
                .licenseNumber(license)
                .groupNo(group)
                .phaseCode(phaseCode)
                .forehand("top")
                .backhand("slice")
                .user(user)
                .build();
        return playerRepository.save(player);
    }
}

