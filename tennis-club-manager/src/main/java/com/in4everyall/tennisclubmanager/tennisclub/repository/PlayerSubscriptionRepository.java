package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerSubscriptionEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerSubscriptionRepository extends JpaRepository<PlayerSubscriptionEntity, UUID> {
    
    List<PlayerSubscriptionEntity> findByPlayer_LicenseNumber(String licenseNumber);
    
    Optional<PlayerSubscriptionEntity> findByPlayer_LicenseNumberAndSubscriptionTypeAndIsActive(
            String licenseNumber, 
            SubscriptionType subscriptionType, 
            Boolean isActive
    );
    
    List<PlayerSubscriptionEntity> findByIsActive(Boolean isActive);
    
    List<PlayerSubscriptionEntity> findByPlayer_LicenseNumberAndIsActive(String licenseNumber, Boolean isActive);
}




