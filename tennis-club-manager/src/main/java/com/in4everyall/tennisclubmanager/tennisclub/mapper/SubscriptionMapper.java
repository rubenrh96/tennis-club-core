package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerSubscriptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    PlayerSubscriptionEntity toEntity(SubscriptionRequest request);
    
    @Mapping(target = "licenseNumber", source = "player.licenseNumber")
    @Mapping(
            target = "playerName",
            expression = "java(subscription.getPlayer() != null && subscription.getPlayer().getUser() != null ? " +
                        "subscription.getPlayer().getUser().getFirstName() + \" \" + subscription.getPlayer().getUser().getLastName() : null)"
    )
    @Mapping(target = "monthlyCost", ignore = true)
    SubscriptionResponse toResponse(PlayerSubscriptionEntity subscription);
}

