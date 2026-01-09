package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RankingService rankingService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final QuarterService quarterService;
    private final ClassTypeService classTypeService;
    private final ClassInstanceService classInstanceService;
    private final HolidayService holidayService;
    private final ClassConsumptionService classConsumptionService;
    private final BulkSubscriptionService bulkSubscriptionService;

    @GetMapping("/phases")
    public List<String> getAllPhases() {
        return matchService.getAllPhaseCodesForAdmin();
    }

    @GetMapping("/players")
    public List<PlayerGroupItemResponse> getAllPlayers(){
        return playerService.getAllPlayers();
    }

    @GetMapping("/groups/{groupNo}/standings")
    public List<StandingRow> getGroupStandings(
            @PathVariable Integer groupNo,
            @RequestParam("phaseCode") String phaseCode
    ) {
        return rankingService.getStandingsForGroup(groupNo, phaseCode);
    }

    @PutMapping("/players/{license}")
    public PlayerGroupItemResponse updatePlayer(@PathVariable String license, @RequestBody PlayerUpdateRequest request
    ) {
        return playerService.updatePlayer(license, request);
    }

    @DeleteMapping("/players/{license}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String license) {
        playerService.deletePlayer(license);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/matches")
    public AdminMatchesSummary getMatchesByPhase(@RequestParam String phaseCode) {
        return adminService.getMatchesByPhase(phaseCode);
    }

    @PostMapping("/matches")
    public MatchResponse addMatchAdmin(@RequestParam String adminLicense,
                                       @RequestBody MatchRequest request                                       ) {
        return matchService.adminAddMatch(adminLicense, request);
    }

    @PostMapping("/matches/confirm-all")
    public void confirmAll(@RequestParam String phaseCode) {
        adminService.confirmAll(phaseCode);
    }

    @PostMapping("/phase/close")
    public ResponseEntity<?> closePhase(@RequestParam String phaseCode) {
        adminService.closePhase(phaseCode);
        return ResponseEntity.ok("Fase cerrada y grupos actualizados");
    }

    @PatchMapping("/matches/{matchId}/cancel")
    public ResponseEntity<MatchResponse> cancelMatch(
            @PathVariable UUID matchId,
            @RequestParam String adminLicense
    ) {
        MatchResponse response = matchService.cancelMatch(matchId, adminLicense);
        return ResponseEntity.ok(response);
    }

    // ========== PAYMENT ENDPOINTS ==========
    
    @GetMapping("/payments")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/payments/player/{licenseNumber}")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse>> getPaymentsByPlayer(
            @PathVariable String licenseNumber
    ) {
        return ResponseEntity.ok(paymentService.getPaymentsByPlayer(licenseNumber));
    }

    @GetMapping("/payments/pending")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse>> getPendingPayments() {
        return ResponseEntity.ok(paymentService.getPendingPayments());
    }

    @GetMapping("/payments/summary")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentSummaryResponse> getPaymentSummary() {
        return ResponseEntity.ok(paymentService.getPaymentSummary());
    }

    @PostMapping("/payments")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse> createPayment(
            @RequestBody com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentRequest request
    ) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PutMapping("/payments/{paymentId}")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse> updatePayment(
            @PathVariable UUID paymentId,
            @RequestBody com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentRequest request
    ) {
        return ResponseEntity.ok(paymentService.updatePayment(paymentId, request));
    }

    @PatchMapping("/payments/{paymentId}/status")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestParam com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus status
    ) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }

    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID paymentId) {
        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    // ========== SUBSCRIPTION ENDPOINTS ==========
    
    @GetMapping("/subscriptions")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/subscriptions/player/{licenseNumber}")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse>> getSubscriptionsByPlayer(
            @PathVariable String licenseNumber
    ) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByPlayer(licenseNumber));
    }

    @GetMapping("/subscriptions/active")
    public ResponseEntity<List<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse>> getActiveSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse> createSubscription(
            @RequestBody com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.createSubscription(request));
    }

    @PutMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse> updateSubscription(
            @PathVariable UUID subscriptionId,
            @RequestBody com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(subscriptionId, request));
    }

    @PatchMapping("/subscriptions/{subscriptionId}/deactivate")
    public ResponseEntity<Void> deactivateSubscription(@PathVariable UUID subscriptionId) {
        subscriptionService.deactivateSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable UUID subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscriptions/bulk")
    public ResponseEntity<BulkSubscriptionResponse> createBulkSubscriptions(
            @RequestBody BulkSubscriptionRequest request
    ) {
        return ResponseEntity.ok(bulkSubscriptionService.createBulkSubscriptions(request));
    }

    @PostMapping("/subscriptions/{subscriptionId}/classes")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse> addClassesToSubscription(
            @PathVariable UUID subscriptionId,
            @RequestBody com.in4everyall.tennisclubmanager.tennisclub.dto.AddClassesToSubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.addClassesToSubscription(subscriptionId, request.classTypeIds()));
    }

    @GetMapping("/subscriptions/{subscriptionId}/bono/consumptions")
    public ResponseEntity<List<ClassConsumptionResponse>> getBonoConsumptions(
            @PathVariable UUID subscriptionId
    ) {
        try {
            List<ClassConsumptionResponse> consumptions = classConsumptionService.getConsumptionsBySubscription(subscriptionId);
            return ResponseEntity.ok(consumptions);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Re-lanzar excepciones de validación con el mismo código
            throw e;
        } catch (Exception e) {
            // Manejar errores inesperados
            System.err.println("Error inesperado al obtener consumos del bono: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    // ========== QUARTER ENDPOINTS ==========
    
    @GetMapping("/quarters")
    public ResponseEntity<List<QuarterResponse>> getAllQuarters() {
        return ResponseEntity.ok(quarterService.getAllQuarters());
    }

    @GetMapping("/quarters/active")
    public ResponseEntity<QuarterResponse> getActiveQuarter() {
        return ResponseEntity.ok(quarterService.getActiveQuarter());
    }

    @GetMapping("/quarters/{id}")
    public ResponseEntity<QuarterResponse> getQuarterById(@PathVariable UUID id) {
        return ResponseEntity.ok(quarterService.getQuarterById(id));
    }

    @PostMapping("/quarters")
    public ResponseEntity<QuarterResponse> createQuarter(@RequestBody QuarterRequest request) {
        return ResponseEntity.ok(quarterService.createQuarter(request));
    }

    @PutMapping("/quarters/{id}")
    public ResponseEntity<QuarterResponse> updateQuarter(
            @PathVariable UUID id,
            @RequestBody QuarterRequest request
    ) {
        return ResponseEntity.ok(quarterService.updateQuarter(id, request));
    }

    @DeleteMapping("/quarters/{id}")
    public ResponseEntity<Void> deleteQuarter(@PathVariable UUID id) {
        quarterService.deleteQuarter(id);
        return ResponseEntity.noContent().build();
    }

    // ========== CLASS TYPE ENDPOINTS ==========
    
    @GetMapping("/class-types")
    public ResponseEntity<List<ClassTypeResponse>> getAllClassTypes() {
        return ResponseEntity.ok(classTypeService.getAllClassTypes());
    }

    @GetMapping("/class-types/active")
    public ResponseEntity<List<ClassTypeResponse>> getActiveClassTypes() {
        return ResponseEntity.ok(classTypeService.getActiveClassTypes());
    }

    @GetMapping("/class-types/{id}")
    public ResponseEntity<ClassTypeResponse> getClassTypeById(@PathVariable UUID id) {
        return ResponseEntity.ok(classTypeService.getClassTypeById(id));
    }

    @PostMapping("/class-types")
    public ResponseEntity<ClassTypeResponse> createClassType(@RequestBody ClassTypeRequest request) {
        return ResponseEntity.ok(classTypeService.createClassType(request));
    }

    @PutMapping("/class-types/{id}")
    public ResponseEntity<ClassTypeResponse> updateClassType(
            @PathVariable UUID id,
            @RequestBody ClassTypeRequest request
    ) {
        return ResponseEntity.ok(classTypeService.updateClassType(id, request));
    }

    @DeleteMapping("/class-types/{id}")
    public ResponseEntity<Void> deleteClassType(@PathVariable UUID id) {
        classTypeService.deleteClassType(id);
        return ResponseEntity.noContent().build();
    }

    // ========== CLASS INSTANCE ENDPOINTS ==========
    
    @GetMapping("/class-instances")
    public ResponseEntity<List<ClassInstanceResponse>> getClassInstances(
            @RequestParam(required = false) UUID quarterId,
            @RequestParam(required = false) UUID classTypeId
    ) {
        if (quarterId != null) {
            return ResponseEntity.ok(classInstanceService.getClassInstancesByQuarter(quarterId));
        } else if (classTypeId != null) {
            return ResponseEntity.ok(classInstanceService.getClassInstancesByClassType(classTypeId));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/class-instances/calendar")
    public ResponseEntity<CalendarResponse> getCalendarForAdmin(
            @RequestParam java.time.LocalDate startDate,
            @RequestParam java.time.LocalDate endDate,
            @RequestParam(required = false) UUID quarterId,
            @RequestParam(required = false) UUID classTypeId
    ) {
        return ResponseEntity.ok(classInstanceService.getCalendarForAdmin(startDate, endDate, quarterId, classTypeId));
    }

    @GetMapping("/class-instances/{id}")
    public ResponseEntity<ClassInstanceResponse> getClassInstanceById(@PathVariable UUID id) {
        return ResponseEntity.ok(classInstanceService.getClassInstanceById(id));
    }

    @PatchMapping("/class-instances/{id}/cancel")
    public ResponseEntity<ClassInstanceResponse> cancelClassInstance(
            @PathVariable UUID id,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(classInstanceService.cancelClassInstance(id, reason));
    }

    @PatchMapping("/class-instances/{id}/complete")
    public ResponseEntity<ClassInstanceResponse> completeClassInstance(@PathVariable UUID id) {
        return ResponseEntity.ok(classInstanceService.completeClassInstance(id));
    }

    @DeleteMapping("/class-instances/{id}")
    public ResponseEntity<Void> deleteClassInstance(@PathVariable UUID id) {
        classInstanceService.deleteClassInstance(id);
        return ResponseEntity.noContent().build();
    }

    // ========== HOLIDAY ENDPOINTS ==========
    
    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayResponse>> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @GetMapping("/holidays/year/{year}")
    public ResponseEntity<List<HolidayResponse>> getHolidaysByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(holidayService.getHolidaysByYear(year));
    }

    @GetMapping("/holidays/{id}")
    public ResponseEntity<HolidayResponse> getHolidayById(@PathVariable UUID id) {
        return ResponseEntity.ok(holidayService.getHolidayById(id));
    }

    @PostMapping("/holidays")
    public ResponseEntity<HolidayResponse> createHoliday(@RequestBody HolidayRequest request) {
        return ResponseEntity.ok(holidayService.createHoliday(request));
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    // ========== CLASS CONSUMPTION ENDPOINTS ==========
    
    @GetMapping("/class-consumptions")
    public ResponseEntity<List<ClassConsumptionResponse>> getClassConsumptions(
            @RequestParam(required = false) UUID subscriptionId
    ) {
        if (subscriptionId != null) {
            try {
                List<ClassConsumptionResponse> consumptions = classConsumptionService.getConsumptionsBySubscription(subscriptionId);
                return ResponseEntity.ok(consumptions);
            } catch (org.springframework.web.server.ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                System.err.println("Error al obtener consumos: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(List.of());
            }
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/class-consumptions/subscription/{subscriptionId}")
    public ResponseEntity<List<ClassConsumptionResponse>> getClassConsumptionsBySubscription(
            @PathVariable UUID subscriptionId
    ) {
        try {
            List<ClassConsumptionResponse> consumptions = classConsumptionService.getConsumptionsBySubscription(subscriptionId);
            return ResponseEntity.ok(consumptions);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error al obtener consumos por suscripción: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @PostMapping("/class-consumptions")
    public ResponseEntity<ClassConsumptionResponse> createClassConsumption(
            @RequestBody ClassConsumptionRequest request
    ) {
        return ResponseEntity.ok(classConsumptionService.consumeClass(request));
    }

    @DeleteMapping("/class-consumptions/{id}")
    public ResponseEntity<Void> deleteClassConsumption(@PathVariable UUID id) {
        classConsumptionService.deleteConsumption(id);
        return ResponseEntity.noContent().build();
    }

}
