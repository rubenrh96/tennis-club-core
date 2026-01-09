package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {
    Optional<PlayerEntity> findByLicenseNumber(String licenseNumber);
    Optional<PlayerEntity> findByLicenseNumberAndPhaseCode(String licenseNumber, String phaseCode);
    List<PlayerEntity> findByGroupNoAndPhaseCode(Integer groupNo, String phaseCode);

    @Query("select p from PlayerEntity p where p.phaseCode = :phaseCode")
    List<PlayerEntity> findByPhaseCode(@Param("phaseCode") String phaseCode);

}
