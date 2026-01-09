package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {

    @Query("""
    select m
    from MatchEntity m
    where (m.player1.licenseNumber = :license
           or m.player2.licenseNumber = :license)
      and m.phaseCode = :phaseCode
    order by m.playedAt desc nulls last, m.scheduledAt desc nulls last
    """)
    List<MatchEntity> findByPlayerAndPhaseCode(
            @Param("license") String license,
            @Param("phaseCode") String phaseCode
    );

    @Query("""
           select count(m) > 0
           from MatchEntity m
           where m.phaseCode = :phaseCode
             and (
                (m.player1.licenseNumber = :p1 and m.player2.licenseNumber = :p2)
                or
                (m.player1.licenseNumber = :p2 and m.player2.licenseNumber = :p1)
             )
           """)
    boolean existsByPhaseCodeAndPlayers(
            @Param("phaseCode") String phaseCode,
            @Param("p1") String p1,
            @Param("p2") String p2
    );

    // 👇 Método para recuperar el partido concreto de una fase entre dos jugadores
    @Query("""
           select m
           from MatchEntity m
           where m.phaseCode = :phaseCode
             and (
                (m.player1.licenseNumber = :p1 and m.player2.licenseNumber = :p2)
                 or
                (m.player1.licenseNumber = :p2 and m.player2.licenseNumber = :p1)
             )
           """)
    Optional<MatchEntity> findByPhaseCodeAndPlayers(
            @Param("phaseCode") String phaseCode,
            @Param("p1") String p1,
            @Param("p2") String p2
    );

    List<MatchEntity> findByPhaseCode(String phaseCode);

    boolean existsByPlayer1_LicenseNumberOrPlayer2_LicenseNumber(String license1, String license2);

    @Query("""
    select count(m)
    from MatchEntity m
    where m.winner.licenseNumber = :license
      and m.phaseCode = :phaseCode
      and m.confirmed = true
      and (m.cancelled is null or m.cancelled = false)
    """)
    int countWins(
            @Param("license") String license,
            @Param("phaseCode") String phaseCode
    );

    @Query("""
        select m from MatchEntity m
        where m.phaseCode = :phaseCode
          and m.confirmed = true
          and (m.cancelled is null or m.cancelled = false)
          and (
               m.player1.licenseNumber in :licenses
            or m.player2.licenseNumber in :licenses
          )
        """)
    List<MatchEntity> findAllConfirmedByPhaseCodeAndPlayers(
            @Param("phaseCode") String phaseCode,
            @Param("licenses") List<String> licenses
    );

    @Query("""
           select m
           from MatchEntity m
           where m.phaseCode = :phaseCode
             and m.groupNoAtMatch = :groupNo
             and m.confirmed = true
             and (m.cancelled is null or m.cancelled = false)
           """)
    List<MatchEntity> findAllConfirmedByPhaseCodeAndGroup(
            @Param("phaseCode") String phaseCode,
            @Param("groupNo") Integer groupNo
    );

    @Query("""
           select distinct m.phaseCode
           from MatchEntity m
           where m.phaseCode is not null
           order by m.phaseCode asc
           """)
    List<String> findAllPhaseCodes();

    @Query("""
           select distinct m.phaseCode
           from MatchEntity m
           where m.phaseCode is not null
             and (m.player1.licenseNumber = :license or m.player2.licenseNumber = :license)
           order by m.phaseCode asc
           """)
    List<String> findPhaseCodesByPlayer(@Param("license") String license);

    @Query("""
           select m
           from MatchEntity m
           where m.player1.licenseNumber = :license or m.player2.licenseNumber = :license
           order by m.playedAt desc nulls last, m.scheduledAt desc nulls last
           """)
    List<MatchEntity> findAllByPlayer(@Param("license") String license);
}

