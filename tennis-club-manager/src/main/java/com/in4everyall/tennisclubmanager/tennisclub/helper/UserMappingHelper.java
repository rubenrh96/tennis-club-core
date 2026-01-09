package com.in4everyall.tennisclubmanager.tennisclub.helper;

import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMappingHelper {

    @Named("stringToLocalDate")
    public LocalDate stringToLocalDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw); // formato ISO: 2025-11-04
    }

    @Named("stringToRole")
    public Role stringToRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return Role.ALUMNO;
        }
        return Role.valueOf(raw.trim().toUpperCase());
    }
}
