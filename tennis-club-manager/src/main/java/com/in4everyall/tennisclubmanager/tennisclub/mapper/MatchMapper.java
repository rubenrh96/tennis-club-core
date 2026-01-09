package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.helper.PlayerMappingHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { PlayerMappingHelper.class })
public interface MatchMapper {

    // Del DTO a la entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player1", ignore = true)  // se setea en el service
    @Mapping(target = "player2", ignore = true)  // se setea en el service
    @Mapping(target = "winner", ignore = true)   // se setea en el service
    MatchEntity toEntity(MatchRequest req);

    // De la entidad al DTO de respuesta
    @Mapping(target = "player1License", source = "player1", qualifiedByName = "playerToLicense")
    @Mapping(target = "player1Name", source = "player1", qualifiedByName = "playerToName")
    @Mapping(target = "player2License", source = "player2", qualifiedByName = "playerToLicense")
    @Mapping(target = "player2Name", source = "player2", qualifiedByName = "playerToName")
    @Mapping(target = "winnerLicense", source = "winner", qualifiedByName = "playerToLicense")
    @Mapping(target = "winnerName", source = "winner", qualifiedByName = "playerToName")
    @Mapping(target = "submittedByLicense", source = "submittedByLicense")
    @Mapping(target = "confirmed", source = "confirmed")
    @Mapping(target = "rejected", source = "rejected")
    @Mapping(target = "cancelled", source = "cancelled")
    @Mapping(target = "status", source = ".", qualifiedByName = "calculateMatchStatus")
    MatchResponse toResponse(MatchEntity match);
}
