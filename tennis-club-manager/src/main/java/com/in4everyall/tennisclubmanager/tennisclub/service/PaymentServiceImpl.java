package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.PaymentMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerSubscriptionRepository;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerSubscriptionEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PlayerRepository playerRepository;
    private final PaymentMapper paymentMapper;
    private final PlayerSubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        PlayerEntity player = playerRepository.findByLicenseNumber(request.licenseNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Jugador no encontrado con licencia: " + request.licenseNumber()));

        PaymentEntity payment = paymentMapper.toEntity(request);
        payment.setPlayer(player);
        payment.setStatus(PaymentStatus.PENDING);

        // Validar y calcular montos según el tipo
        validateAndSetPaymentDetails(payment, request);

        PaymentEntity savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(UUID paymentId, PaymentRequest request) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Pago no encontrado"));

        PlayerEntity player = playerRepository.findByLicenseNumber(request.licenseNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Jugador no encontrado"));

        payment.setPlayer(player);
        payment.setPaymentType(request.paymentType());
        payment.setAmount(request.amount());
        payment.setPaymentDate(request.paymentDate());
        payment.setClassDate(request.classDate());
        payment.setDaysPerWeek(request.daysPerWeek());
        payment.setYear(request.year());
        payment.setQuarterNumber(request.quarterNumber());
        payment.setNotes(request.notes());

        validateAndSetPaymentDetails(payment, request);

        PaymentEntity updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Pago no encontrado"));

        payment.setStatus(status);
        PaymentEntity updatedPayment = paymentRepository.save(payment);
        
        // Si el pago está vinculado a una suscripción y se marca como PAGADO, activar la suscripción
        if (status == PaymentStatus.PAID && payment.getSubscription() != null) {
            activateSubscriptionForPayment(payment.getSubscription());
        }
        
        return paymentMapper.toResponse(updatedPayment);
    }
    
    /**
     * Actualiza la suscripción cuando se marca el pago asociado como PAGADO
     * Nota: La suscripción ya está activa desde su creación, pero aquí se pueden
     * actualizar otros campos como la fecha de compra del bono
     */
    private void activateSubscriptionForPayment(PlayerSubscriptionEntity subscription) {
        // Si es un bono, establecer la fecha de compra si no está establecida
        if (subscription.getSubscriptionType() == com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType.CLASS_PACKAGE 
                && subscription.getPackagePurchaseDate() == null) {
            subscription.setPackagePurchaseDate(java.time.LocalDate.now());
            subscriptionRepository.save(subscription);
        }
        // La suscripción ya está activa desde su creación, no es necesario activarla aquí
    }

    @Override
    public PaymentResponse getPaymentById(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Pago no encontrado"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByPlayer(String licenseNumber) {
        return paymentRepository.findByPlayer_LicenseNumberOrderByPaymentDateDesc(licenseNumber).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPendingPaymentsByPlayer(String licenseNumber) {
        return paymentRepository.findPendingPaymentsByPlayer(licenseNumber, PaymentStatus.PENDING).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentSummaryResponse getPaymentSummary() {
        List<PaymentEntity> allPayments = paymentRepository.findAll();
        
        BigDecimal totalPaid = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        List<PaymentByTypeSummary> byType = calculateByTypeSummary(allPayments);

        return new PaymentSummaryResponse(totalPaid, totalPending, (int) pendingCount, byType);
    }

    @Override
    public PaymentSummaryResponse getPaymentSummaryByPlayer(String licenseNumber) {
        List<PaymentEntity> playerPayments = paymentRepository.findByPlayer_LicenseNumber(licenseNumber);
        
        BigDecimal totalPaid = paymentRepository.getTotalPaidByPlayer(licenseNumber);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        BigDecimal totalPending = paymentRepository.getTotalPendingByPlayer(licenseNumber);
        if (totalPending == null) totalPending = BigDecimal.ZERO;

        long pendingCount = playerPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        List<PaymentByTypeSummary> byType = calculateByTypeSummary(playerPayments);

        return new PaymentSummaryResponse(totalPaid, totalPending, (int) pendingCount, byType);
    }

    @Override
    public List<PaymentResponse> getPaymentsByType(PaymentType paymentType) {
        return paymentRepository.findByPaymentType(paymentType).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayment(UUID paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado");
        }
        paymentRepository.deleteById(paymentId);
    }

    private void validateAndSetPaymentDetails(PaymentEntity payment, PaymentRequest request) {
        switch (request.paymentType()) {
            case INDIVIDUAL_CLASS:
                // Clase individual: 30 euros
                if (request.amount().compareTo(new BigDecimal("30")) != 0) {
                    payment.setAmount(new BigDecimal("30")); // Forzar el precio correcto
                }
                if (request.classDate() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "La fecha de clase es obligatoria para clases individuales");
                }
                payment.setClassDate(request.classDate());
                break;

            case CLASS_PACKAGE:
                // Bono de 10 clases: 280 euros
                if (request.amount().compareTo(new BigDecimal("280")) != 0) {
                    payment.setAmount(new BigDecimal("280")); // Forzar el precio correcto
                }
                payment.setClassesRemaining(10);
                break;

            case QUARTERLY:
                // Trimestre: 140 euros × días por semana
                if (request.daysPerWeek() == null || request.daysPerWeek() < 1 || request.daysPerWeek() > 5) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Los días por semana deben estar entre 1 y 5");
                }
                BigDecimal quarterlyAmount = new BigDecimal("140").multiply(new BigDecimal(request.daysPerWeek()));
                payment.setAmount(quarterlyAmount);
                payment.setDaysPerWeek(request.daysPerWeek());
                
                // Calcular fechas del trimestre
                if (request.year() != null && request.quarterNumber() != null) {
                    LocalDate[] quarterDates = calculateQuarterDates(request.year(), request.quarterNumber());
                    payment.setQuarterStartDate(quarterDates[0]);
                    payment.setQuarterEndDate(quarterDates[1]);
                    payment.setYear(request.year());
                    payment.setQuarterNumber(request.quarterNumber());
                }
                break;
        }
    }

    private LocalDate[] calculateQuarterDates(int year, int quarter) {
        LocalDate startDate;
        LocalDate endDate;

        switch (quarter) {
            case 1:
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 3, 31);
                break;
            case 2:
                startDate = LocalDate.of(year, 4, 1);
                endDate = LocalDate.of(year, 6, 30);
                break;
            case 3:
                startDate = LocalDate.of(year, 9, 1);
                endDate = LocalDate.of(year, 12, 31);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "El número de trimestre debe ser 1, 2 o 3");
        }

        return new LocalDate[]{startDate, endDate};
    }

    private List<PaymentByTypeSummary> calculateByTypeSummary(List<PaymentEntity> payments) {
        List<PaymentByTypeSummary> summaries = new ArrayList<>();

        for (PaymentType type : PaymentType.values()) {
            List<PaymentEntity> typePayments = payments.stream()
                    .filter(p -> p.getPaymentType() == type)
                    .collect(Collectors.toList());

            BigDecimal totalPaid = typePayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PAID)
                    .map(PaymentEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPending = typePayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .map(PaymentEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            summaries.add(new PaymentByTypeSummary(type, totalPaid, totalPending, typePayments.size()));
        }

        return summaries;
    }
}



