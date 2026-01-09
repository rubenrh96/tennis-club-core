package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.BulkSubscriptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.BulkSubscriptionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkSubscriptionService {

    private final SubscriptionService subscriptionService;
    private final ClassInstanceService classInstanceService;
    private final PaymentRepository paymentRepository;
    private final com.in4everyall.tennisclubmanager.tennisclub.mapper.PaymentMapper paymentMapper;

    @Transactional
    public BulkSubscriptionResponse createBulkSubscriptions(BulkSubscriptionRequest request) {
        List<SubscriptionResponse> createdSubscriptions = new ArrayList<>();
        List<PaymentResponse> createdPayments = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (String licenseNumber : request.licenseNumbers()) {
            try {
                // Crear suscripción para este jugador
                List<UUID> classTypeIds = List.of(request.classTypeId());
                SubscriptionRequest subRequest = new SubscriptionRequest(
                        licenseNumber,
                        SubscriptionType.QUARTERLY,
                        null, // classesRemaining
                        null, // packagePurchaseDate
                        1, // daysPerWeek (1 clase seleccionada)
                        request.quarterStartDate(),
                        request.quarterEndDate(),
                        classTypeIds, // classTypeIds (incluir el tipo de clase)
                        null // autoRenew
                );

                // La suscripción se crea con classTypeIds, y el servicio generará automáticamente las instancias
                SubscriptionResponse subscription = subscriptionService.createSubscription(subRequest);

                // Obtener el pago creado automáticamente
                PaymentEntity payment = paymentRepository.findBySubscription_Id(subscription.id())
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Pago no encontrado para la suscripción creada"));

                createdSubscriptions.add(subscription);
                createdPayments.add(paymentMapper.toResponse(payment));

            } catch (Exception e) {
                errors.add("Error para jugador " + licenseNumber + ": " + e.getMessage());
            }
        }

        return new BulkSubscriptionResponse(
                createdSubscriptions.size(),
                createdSubscriptions,
                createdPayments,
                errors
        );
    }
}

