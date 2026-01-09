package com.in4everyall.tennisclubmanager.tennisclub.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidades para construir y parsear identificadores de fase ({@code phaseCode})
 * con el formato {@code "YYYY-F"} (por ejemplo {@code "2025-1"}).
 */
public final class PhaseCodeUtils {

    private static final Pattern PHASE_CODE_PATTERN = Pattern.compile("^(\\d{4})-(\\d+)$");

    private PhaseCodeUtils() {
    }

    public static String buildPhaseCode(int year, int phaseNumber) {
        if (phaseNumber <= 0) {
            throw new IllegalArgumentException("phaseNumber must be positive");
        }
        return year + "-" + phaseNumber;
    }

    public static PhaseComponents parse(String phaseCode) {
        if (phaseCode == null) {
            throw new IllegalArgumentException("phaseCode cannot be null");
        }
        Matcher matcher = PHASE_CODE_PATTERN.matcher(phaseCode);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid phaseCode format: " + phaseCode);
        }
        int year = Integer.parseInt(matcher.group(1));
        int phaseNumber = Integer.parseInt(matcher.group(2));
        return new PhaseComponents(year, phaseNumber);
    }

    public static int extractYear(String phaseCode) {
        return parse(phaseCode).year();
    }

    public static int extractPhaseNumber(String phaseCode) {
        return parse(phaseCode).phaseNumber();
    }

    /**
     * Componentes básicos de un phaseCode.
     */
    public record PhaseComponents(int year, int phaseNumber) {
    }
}

