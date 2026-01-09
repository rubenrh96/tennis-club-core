package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final RankingService rankingService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final ClassInstanceService classInstanceService;
    private final ClassConsumptionService classConsumptionService;

    @GetMapping("/my-group")
    public ResponseEntity<List<PlayerGroupItemResponse>> getMyGroup(@RequestParam String license) {
        return ResponseEntity.ok(playerService.getMyGroupPlayers(license));
    }

    /**
     * Devuelve la clasificación completa del grupo del jugador
     * para un mes concreto (phaseMonth), ya ordenada y con posición.
     *
     * Ejemplo llamada:
     *   GET /api/players/LIC-12345/standings?phaseCode=2025-1
     */
    @GetMapping("/{licenseNumber}/standings")
    public List<StandingRow> getPlayerStandings(
            @PathVariable String licenseNumber,
            @RequestParam("phaseCode") String phaseCode
    ) {
        return rankingService.getStandingsForPlayerDashboard(licenseNumber, phaseCode);
    }

    // ========== PAYMENT ENDPOINTS ==========
    
    @GetMapping("/my-payments")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse>> getMyPayments(
            @RequestParam String license
    ) {
        return ResponseEntity.ok(paymentService.getPaymentsByPlayer(license));
    }

    @GetMapping("/my-payments/pending")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse>> getMyPendingPayments(
            @RequestParam String license
    ) {
        return ResponseEntity.ok(paymentService.getPendingPaymentsByPlayer(license));
    }

    @GetMapping("/my-payments/summary")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentSummaryResponse> getMyPaymentSummary(
            @RequestParam String license
    ) {
        return ResponseEntity.ok(paymentService.getPaymentSummaryByPlayer(license));
    }

    // ========== SUBSCRIPTION ENDPOINTS ==========
    
    @GetMapping("/my-subscription")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse> getMySubscription(
            @RequestParam String license
    ) {
        try {
            return ResponseEntity.ok(subscriptionService.getActiveSubscriptionByPlayer(license));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode().value() == 404) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    // ========== CLASS CALENDAR ENDPOINTS ==========
    
    @GetMapping("/my-classes/calendar")
    public ResponseEntity<CalendarResponse> getMyClassCalendar(
            @RequestParam String license,
            @RequestParam java.time.LocalDate startDate,
            @RequestParam java.time.LocalDate endDate
    ) {
        return ResponseEntity.ok(classInstanceService.getCalendarForPlayer(license, startDate, endDate));
    }

    @GetMapping("/class-instances/calendar")
    public ResponseEntity<CalendarResponse> getClassInstancesCalendar(
            @RequestParam String license,
            @RequestParam java.time.LocalDate startDate,
            @RequestParam java.time.LocalDate endDate,
            @RequestParam(required = false) String view
    ) {
        return ResponseEntity.ok(classInstanceService.getCalendarForPlayer(license, startDate, endDate));
    }

    @GetMapping("/my-classes/bono/consumptions")
    public ResponseEntity<List<ClassConsumptionResponse>> getMyBonoConsumptions(
            @RequestParam String license
    ) {
        return ResponseEntity.ok(classConsumptionService.getConsumptionsByPlayer(license));
    }
}
