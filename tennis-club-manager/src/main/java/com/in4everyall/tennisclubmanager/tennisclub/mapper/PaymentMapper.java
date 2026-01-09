package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "classSessionId", ignore = true)
    PaymentEntity toEntity(PaymentRequest request);
    
    @Mapping(target = "playerLicenseNumber", source = "player.licenseNumber")
    @Mapping(
            target = "playerName",
            expression = "java(payment.getPlayer() != null && payment.getPlayer().getUser() != null ? " +
                        "payment.getPlayer().getUser().getFirstName() + \" \" + payment.getPlayer().getUser().getLastName() : null)"
    )
    @Mapping(
            target = "subscriptionId",
            expression = "java(payment.getSubscription() != null ? payment.getSubscription().getId() : null)"
    )
    PaymentResponse toResponse(PaymentEntity payment);
}

