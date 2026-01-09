package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.config.AuditConfig;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.type.preferred_enum_type=string"
})
@Import(AuditConfig.class)
class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;

    private PlayerEntity player1;
    private PlayerEntity player2;
    private PlayerEntity player3;
    private final String march = "2025-3";

    @BeforeEach
    void setUp() {
        player1 = persistPlayer("P1", "Ana", "López", 1, march);
        player2 = persistPlayer("P2", "Bea", "Ruiz", 1, march);
        player3 = persistPlayer("P3", "Carlos", "Gómez", 1, march);
    }

    @Test
    void shouldFindMatchesByPlayerAndMonthRegardlessOfOrder() {
        MatchEntity match = matchRepository.save(match(player1, player2, march, true, 1));

        List<MatchEntity> forP1 = matchRepository.findByPlayerAndPhaseCode("P1", march);
        List<MatchEntity> forP2 = matchRepository.findByPlayerAndPhaseCode("P2", march);

        assertThat(forP1).containsExactly(match);
        assertThat(forP2).containsExactly(match);
    }

    @Test
    void shouldDetectExistingMatchRegardlessOfPlayerOrder() {
        matchRepository.save(match(player1, player2, march, true, 1));

        assertThat(matchRepository.existsByPhaseCodeAndPlayers(march, "P1", "P2")).isTrue();
        assertThat(matchRepository.existsByPhaseCodeAndPlayers(march, "P2", "P1")).isTrue();
    }

    @Test
    void shouldReturnConfirmedMatchesForGroupOfPlayers() {
        MatchEntity confirmed = matchRepository.save(match(player1, player2, march, true, 1));
        matchRepository.save(match(player1, player3, march, false, 1));

        List<MatchEntity> results = matchRepository.findAllConfirmedByPhaseCodeAndPlayers(
                march,
                List.of("P1", "P2")
        );

        assertThat(results).containsExactly(confirmed);
    }

    @Test
    void shouldCountWinsForPhaseMonth() {
        MatchEntity won = match(player1, player2, march, true, 1);
        won.setWinner(player1);
        matchRepository.save(won);

        MatchEntity lost = match(player1, player3, march, true, 1);
        lost.setWinner(player3);
        matchRepository.save(lost);

        assertThat(matchRepository.countWins("P1", march)).isEqualTo(1);
        assertThat(matchRepository.countWins("P3", march)).isEqualTo(1);
    }

    @Test
    void shouldReturnConfirmedMatchesByPhaseAndGroupNoAtMatch() {
        MatchEntity inGroup1 = matchRepository.save(match(player1, player2, march, true, 1));
        matchRepository.save(match(player1, player2, march, true, 2)); // otro grupo

        List<MatchEntity> results = matchRepository.findAllConfirmedByPhaseCodeAndGroup(march, 1);

        assertThat(results).containsExactly(inGroup1);
    }

    private MatchEntity match(PlayerEntity p1, PlayerEntity p2, String phaseCode, boolean confirmed, Integer groupNo) {
        return MatchEntity.builder()
                .phaseCode(phaseCode)
                .groupNoAtMatch(groupNo)
                .scheduledAt(Instant.parse("2025-03-05T10:00:00Z"))
                .playedAt(Instant.parse("2025-03-06T10:00:00Z"))
                .player1(p1)
                .player2(p2)
                .winner(p1)
                .set1P1((short) 6).set1P2((short) 3)
                .set2P1((short) 6).set2P2((short) 4)
                .submittedByLicense(p1.getLicenseNumber())
                .confirmed(confirmed)
                .rejected(false)
                .build();
    }

    private PlayerEntity persistPlayer(String license, String firstName, String lastName, int group, String phaseCode) {
        UserEntity user = UserEntity.builder()
                .email(license.toLowerCase() + "@club.test")
                .licenseNumber(license)
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(LocalDate.of(1990, 1, 1))
                .passwordHash("pwd")
                .role(Role.PLAYER)
                .phone("+34999999999")
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

