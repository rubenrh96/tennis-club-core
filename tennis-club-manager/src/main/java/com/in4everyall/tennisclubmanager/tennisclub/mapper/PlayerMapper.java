package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.PlayerGroupItemResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerMapper {

    PlayerEntity toEntity(SignUpRequest req);

    @Mapping(target = "license", source = "licenseNumber")
    @Mapping(
            target = "fullName",
            expression = "java(player.getUser() != null ? player.getUser().getFirstName() + \" \" + player.getUser().getLastName() : null)"
    )
    @Mapping(target = "groupNo", source = "groupNo")
    @Mapping(
            target = "phaseCode",
            expression = "java(player.getPhaseCode())"
    )
    @Mapping(
            target = "phone",
            expression = "java(player.getUser() != null ? player.getUser().getPhone() : null)"
    )
    PlayerGroupItemResponse toGroupItemResponse(PlayerEntity player);
}
