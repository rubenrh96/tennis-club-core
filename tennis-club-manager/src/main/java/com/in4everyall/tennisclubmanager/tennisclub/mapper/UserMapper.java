package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.helper.UserMappingHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = { UserMappingHelper.class }
)
public interface UserMapper {

    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    @Mapping(target = "phone", source = "phone")
    UserEntity toEntity(SignUpRequest req);

    UserResponse toResponse(UserEntity user);

    @Mapping(target = "token", expression = "java(token)")
    UserResponse toResponse(UserEntity user, String token);
}
