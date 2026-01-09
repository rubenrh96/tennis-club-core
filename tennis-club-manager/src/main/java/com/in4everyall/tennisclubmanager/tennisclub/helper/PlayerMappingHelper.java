package com.in4everyall.tennisclubmanager.tennisclub.helper;

import com.in4everyall.tennisclubmanager.tennisclub.entity.MatchEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class PlayerMappingHelper {

    @Named("playerToLicense")
    public String playerToLicense(PlayerEntity player) {
        return player != null ? player.getLicenseNumber() : null;
    }

    @Named("playerToName")
    public String playerToName(PlayerEntity player) {
        if (player == null || player.getUser() == null) return null;
        return player.getUser().getFirstName() + " " + player.getUser().getLastName();
    }

    @Named("calculateMatchStatus")
    public String calculateMatchStatus(MatchEntity match) {
        if (match == null) return "PENDING";
        
        if (Boolean.TRUE.equals(match.getCancelled())) {
            return "CANCELLED";
        }
        if (match.isConfirmed()) {
            return "CONFIRMED";
        }
        if (match.isRejected()) {
            return "REJECTED";
        }
        return "PENDING";
    }
}

