package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ClassTypeEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerSubscriptionEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerClassEnrollmentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.SubscriptionMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerSubscriptionRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ClassTypeRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerClassEnrollmentRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.QuarterRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ClassConsumptionRepository;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ClassConsumptionEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.QuarterEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.service.ClassInstanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final PlayerSubscriptionRepository subscriptionRepository;
    private final PlayerRepository playerRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PaymentRepository paymentRepository;
    private final ClassInstanceService classInstanceService;
    private final ClassTypeRepository classTypeRepository;
    private final PlayerClassEnrollmentRepository enrollmentRepository;
    private final QuarterRepository quarterRepository;
    private final ClassConsumptionRepository consumptionRepository;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        PlayerEntity player = playerRepository.findByLicenseNumber(request.licenseNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Jugador no encontrado con licencia: " + request.licenseNumber()));

        // Verificar si ya existe una suscripción activa del mismo tipo
        subscriptionRepository.findByPlayer_LicenseNumberAndSubscriptionTypeAndIsActive(
                request.licenseNumber(), 
                request.subscriptionType(), 
                true
        ).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Ya existe una suscripción activa de tipo " + request.subscriptionType() + 
                    " para este jugador");
        });

        PlayerSubscriptionEntity subscription = subscriptionMapper.toEntity(request);
        subscription.setPlayer(player);
        subscription.setIsActive(true);

        // Validar y establecer detalles según el tipo
        validateAndSetSubscriptionDetails(subscription, request);

        // La suscripción se crea ACTIVA (el admin la crea y el jugador pagará después)
        subscription.setIsActive(true);

        PlayerSubscriptionEntity savedSubscription = subscriptionRepository.save(subscription);
        
        // Para suscripciones trimestrales: crear enrollments y generar instancias de clase
        if (request.subscriptionType() == SubscriptionType.QUARTERLY && 
            request.classTypeIds() != null && !request.classTypeIds().isEmpty() &&
            request.currentQuarterStart() != null && request.currentQuarterEnd() != null) {
            
            // Obtener o crear el trimestre
            QuarterEntity quarter = quarterRepository.findByDateRange(
                    request.currentQuarterStart(), 
                    request.currentQuarterEnd()
            ).stream()
            .findFirst()
            .orElseGet(() -> {
                QuarterEntity newQuarter = QuarterEntity.builder()
                        .name("Q" + getQuarterNumber(request.currentQuarterStart()) + " " + request.currentQuarterStart().getYear())
                        .startDate(request.currentQuarterStart())
                        .endDate(request.currentQuarterEnd())
                        .isActive(true)
                        .build();
                return quarterRepository.save(newQuarter);
            });
            
            // Crear enrollments (inscripciones del jugador a los tipos de clases)
            for (UUID classTypeId : request.classTypeIds()) {
                // Verificar que el tipo de clase existe
                var classType = classTypeRepository.findById(classTypeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Tipo de clase no encontrado: " + classTypeId));
                
                // Crear enrollment
                PlayerClassEnrollmentEntity enrollment = PlayerClassEnrollmentEntity.builder()
                        .player(player)
                        .subscription(savedSubscription)
                        .classType(classType)
                        .quarter(quarter)
                        .isActive(true)
                        .build();
                enrollmentRepository.save(enrollment);
            }
            
            // Generar instancias de clase automáticamente
            classInstanceService.generateInstancesForSubscription(
                    savedSubscription.getId(),
                    request.classTypeIds(),
                    request.currentQuarterStart(),
                    request.currentQuarterEnd()
            );
        }
        
        // Generar automáticamente el pago asociado a la suscripción (excepto para INDIVIDUAL_CLASSES)
        if (request.subscriptionType() != SubscriptionType.INDIVIDUAL_CLASSES) {
            createPaymentForSubscription(savedSubscription, request);
        }
        
        SubscriptionResponse response = subscriptionMapper.toResponse(savedSubscription);
        return enrichWithMonthlyCost(response, savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscription(UUID subscriptionId, SubscriptionRequest request) {
        PlayerSubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Suscripción no encontrada"));

        PlayerEntity player = playerRepository.findByLicenseNumber(request.licenseNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Jugador no encontrado"));

        subscription.setPlayer(player);
        subscription.setSubscriptionType(request.subscriptionType());
        subscription.setClassesRemaining(request.classesRemaining());
        subscription.setPackagePurchaseDate(request.packagePurchaseDate());
        subscription.setDaysPerWeek(request.daysPerWeek());
        subscription.setCurrentQuarterStart(request.currentQuarterStart());
        subscription.setCurrentQuarterEnd(request.currentQuarterEnd());
        subscription.setAutoRenew(request.autoRenew() != null ? request.autoRenew() : false);

        validateAndSetSubscriptionDetails(subscription, request);

        PlayerSubscriptionEntity updatedSubscription = subscriptionRepository.save(subscription);
        SubscriptionResponse response = subscriptionMapper.toResponse(updatedSubscription);
        return enrichWithMonthlyCost(response, updatedSubscription);
    }

    @Override
    public SubscriptionResponse getSubscriptionById(UUID subscriptionId) {
        PlayerSubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Suscripción no encontrada"));
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);
        return enrichWithMonthlyCost(response, subscription);
    }

    @Override
    public SubscriptionResponse getActiveSubscriptionByPlayer(String licenseNumber) {
        List<PlayerSubscriptionEntity> activeSubscriptions = subscriptionRepository
                .findByPlayer_LicenseNumberAndIsActive(licenseNumber, true);
        
        if (activeSubscriptions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "No se encontró suscripción activa para el jugador");
        }
        
        // Retornar la primera suscripción activa (o la más reciente)
        PlayerSubscriptionEntity subscription = activeSubscriptions.get(0);
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);
        return enrichWithMonthlyCost(response, subscription);
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(sub -> {
                    SubscriptionResponse response = subscriptionMapper.toResponse(sub);
                    return enrichWithMonthlyCost(response, sub);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionsByPlayer(String licenseNumber) {
        return subscriptionRepository.findByPlayer_LicenseNumber(licenseNumber).stream()
                .map(sub -> {
                    SubscriptionResponse response = subscriptionMapper.toResponse(sub);
                    return enrichWithMonthlyCost(response, sub);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getActiveSubscriptions() {
        return subscriptionRepository.findByIsActive(true).stream()
                .map(sub -> {
                    SubscriptionResponse response = subscriptionMapper.toResponse(sub);
                    return enrichWithMonthlyCost(response, sub);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateSubscription(UUID subscriptionId) {
        PlayerSubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Suscripción no encontrada"));
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void deleteSubscription(UUID subscriptionId) {
        // Validar que la suscripción existe
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Suscripción no encontrada");
        }

        // 1. Eliminar pagos asociados a la suscripción
        List<PaymentEntity> payments = paymentRepository.findBySubscription_Id(subscriptionId);
        if (!payments.isEmpty()) {
            paymentRepository.deleteAll(payments);
        }

        // 2. Eliminar inscripciones a clases (PlayerClassEnrollmentEntity)
        // Esto hará que el jugador ya no aparezca en el calendario para esas clases
        List<PlayerClassEnrollmentEntity> enrollments = enrollmentRepository.findBySubscription_Id(subscriptionId);
        if (!enrollments.isEmpty()) {
            enrollmentRepository.deleteAll(enrollments);
        }

        // 3. Eliminar consumos de clases (ClassConsumptionEntity) si es un bono
        // Los consumos de bonos están relacionados con la suscripción
        List<ClassConsumptionEntity> consumptions = consumptionRepository.findBySubscription_Id(subscriptionId);
        if (!consumptions.isEmpty()) {
            consumptionRepository.deleteAll(consumptions);
        }

        // 4. Finalmente, eliminar la suscripción
        // Nota: Las instancias de clase (ClassInstanceEntity) NO se eliminan porque son compartidas
        // entre todos los jugadores inscritos en ese tipo de clase y trimestre.
        // Al eliminar las inscripciones (PlayerClassEnrollmentEntity), el jugador ya no aparecerá
        // en el calendario para esas clases.
        subscriptionRepository.deleteById(subscriptionId);
    }

    private void validateAndSetSubscriptionDetails(PlayerSubscriptionEntity subscription, SubscriptionRequest request) {
        switch (request.subscriptionType()) {
            case CLASS_PACKAGE:
                if (request.classesRemaining() == null || request.classesRemaining() < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "El número de clases restantes debe ser mayor o igual a 0");
                }
                subscription.setClassesRemaining(request.classesRemaining());
                if (request.packagePurchaseDate() != null) {
                    subscription.setPackagePurchaseDate(request.packagePurchaseDate());
                } else {
                    subscription.setPackagePurchaseDate(java.time.LocalDate.now());
                }
                break;

            case QUARTERLY:
                if (request.daysPerWeek() == null || request.daysPerWeek() < 1 || request.daysPerWeek() > 5) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Los días por semana deben estar entre 1 y 5");
                }
                subscription.setDaysPerWeek(request.daysPerWeek());
                if (request.currentQuarterStart() != null && request.currentQuarterEnd() != null) {
                    subscription.setCurrentQuarterStart(request.currentQuarterStart());
                    subscription.setCurrentQuarterEnd(request.currentQuarterEnd());
                }
                break;

            case INDIVIDUAL_CLASSES:
                // No requiere campos adicionales
                break;
        }
    }

    private SubscriptionResponse enrichWithMonthlyCost(SubscriptionResponse response, PlayerSubscriptionEntity subscription) {
        BigDecimal monthlyCost = calculateMonthlyCost(subscription);
        return new SubscriptionResponse(
                response.id(),
                response.licenseNumber(),
                response.playerName(),
                response.subscriptionType(),
                response.isActive(),
                response.classesRemaining(),
                response.daysPerWeek(),
                response.currentQuarterStart(),
                response.currentQuarterEnd(),
                monthlyCost,
                response.autoRenew()
        );
    }

    private BigDecimal calculateMonthlyCost(PlayerSubscriptionEntity subscription) {
        if (subscription.getSubscriptionType() == SubscriptionType.QUARTERLY && subscription.getDaysPerWeek() != null) {
            // Trimestre: 140 euros × días por semana / 3 meses
            return new BigDecimal("140")
                    .multiply(new BigDecimal(subscription.getDaysPerWeek()))
                    .divide(new BigDecimal("3"), 2, java.math.RoundingMode.HALF_UP);
        } else if (subscription.getSubscriptionType() == SubscriptionType.CLASS_PACKAGE) {
            // Bono: 280 euros / 10 clases = 28 euros por clase
            return new BigDecimal("28");
        } else if (subscription.getSubscriptionType() == SubscriptionType.INDIVIDUAL_CLASSES) {
            // Clase individual: 30 euros
            return new BigDecimal("30");
        }
        return BigDecimal.ZERO;
    }

    /**
     * Crea automáticamente un pago pendiente asociado a la suscripción creada
     */
    private void createPaymentForSubscription(PlayerSubscriptionEntity subscription, SubscriptionRequest request) {
        PaymentEntity payment = PaymentEntity.builder()
                .player(subscription.getPlayer())
                .subscription(subscription)
                .status(PaymentStatus.PENDING)
                .paymentDate(java.time.LocalDate.now())
                .build();

        // Calcular monto y tipo de pago según el tipo de suscripción
        switch (request.subscriptionType()) {
            case CLASS_PACKAGE:
                payment.setPaymentType(PaymentType.CLASS_PACKAGE);
                payment.setAmount(new BigDecimal("280.00"));
                payment.setClassesRemaining(10);
                payment.setNotes("Bono de 10 clases - Generado automáticamente al crear suscripción");
                break;

            case QUARTERLY:
                payment.setPaymentType(PaymentType.QUARTERLY);
                if (request.daysPerWeek() != null) {
                    BigDecimal amount = new BigDecimal("140").multiply(new BigDecimal(request.daysPerWeek()));
                    payment.setAmount(amount);
                    payment.setDaysPerWeek(request.daysPerWeek());
                }
                if (request.currentQuarterStart() != null) {
                    payment.setQuarterStartDate(request.currentQuarterStart());
                }
                if (request.currentQuarterEnd() != null) {
                    payment.setQuarterEndDate(request.currentQuarterEnd());
                }
                // Calcular año y trimestre desde las fechas
                if (request.currentQuarterStart() != null) {
                    payment.setYear(request.currentQuarterStart().getYear());
                    int month = request.currentQuarterStart().getMonthValue();
                    if (month >= 1 && month <= 3) {
                        payment.setQuarterNumber(1);
                    } else if (month >= 4 && month <= 6) {
                        payment.setQuarterNumber(2);
                    } else if (month >= 9 && month <= 12) {
                        payment.setQuarterNumber(3);
                    }
                }
                payment.setNotes("Trimestre - Generado automáticamente al crear suscripción");
                break;

            case INDIVIDUAL_CLASSES:
                // No se genera pago para clases individuales
                return;
        }

        paymentRepository.save(payment);
    }
    
    private int getQuarterNumber(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 1 && month <= 3) return 1;
        if (month >= 4 && month <= 6) return 2;
        if (month >= 7 && month <= 9) return 3;
        return 4;
    }

    @Override
    @Transactional
    public SubscriptionResponse addClassesToSubscription(UUID subscriptionId, List<UUID> classTypeIds) {
        // Validar que la suscripción existe y es QUARTERLY
        PlayerSubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Suscripción no encontrada"));

        if (subscription.getSubscriptionType() != SubscriptionType.QUARTERLY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Solo se pueden añadir clases a suscripciones trimestrales");
        }

        if (!subscription.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La suscripción no está activa");
        }

        if (subscription.getCurrentQuarterStart() == null || subscription.getCurrentQuarterEnd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La suscripción no tiene fechas de trimestre definidas");
        }

        // Obtener el trimestre
        QuarterEntity quarter = quarterRepository.findByDateRange(
                subscription.getCurrentQuarterStart(), 
                subscription.getCurrentQuarterEnd()
        ).stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Trimestre no encontrado"));

        // Validar y filtrar tipos de clase
        List<UUID> newClassTypeIds = new java.util.ArrayList<>();
        for (UUID classTypeId : classTypeIds) {
            ClassTypeEntity classType = classTypeRepository.findById(classTypeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Tipo de clase no encontrado: " + classTypeId));

            if (!classType.getIsActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "El tipo de clase no está activo: " + classTypeId);
            }

            // Verificar si el jugador ya está inscrito en este tipo de clase
            boolean alreadyEnrolled = enrollmentRepository.findBySubscription_Id(subscriptionId)
                    .stream()
                    .anyMatch(e -> e.getClassType().getId().equals(classTypeId) && e.getIsActive());

            if (!alreadyEnrolled) {
                newClassTypeIds.add(classTypeId);
            }
        }

        if (newClassTypeIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Todos los tipos de clase ya están añadidos a la suscripción");
        }

        // Crear enrollments para los nuevos tipos de clase
        PlayerEntity player = subscription.getPlayer();
        for (UUID classTypeId : newClassTypeIds) {
            ClassTypeEntity classType = classTypeRepository.findById(classTypeId)
                    .orElseThrow();

            PlayerClassEnrollmentEntity enrollment = PlayerClassEnrollmentEntity.builder()
                    .player(player)
                    .subscription(subscription)
                    .classType(classType)
                    .quarter(quarter)
                    .isActive(true)
                    .build();
            enrollmentRepository.save(enrollment);
        }

        // Generar instancias de clase desde hoy hasta el fin del trimestre
        LocalDate today = LocalDate.now();
        LocalDate quarterEnd = subscription.getCurrentQuarterEnd();
        LocalDate startDate = today.isBefore(subscription.getCurrentQuarterStart()) 
                ? subscription.getCurrentQuarterStart() 
                : today;

        classInstanceService.generateInstancesForSubscription(
                subscriptionId,
                newClassTypeIds,
                startDate,
                quarterEnd
        );

        // Actualizar daysPerWeek
        long totalActiveEnrollments = enrollmentRepository.findBySubscription_Id(subscriptionId)
                .stream()
                .filter(e -> e.getIsActive())
                .count();
        subscription.setDaysPerWeek((int) totalActiveEnrollments);
        subscriptionRepository.save(subscription);

        // Actualizar o crear el pago asociado
        updatePaymentForAdditionalClasses(subscription, newClassTypeIds);

        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);
        return enrichWithMonthlyCost(response, subscription);
    }

    private void updatePaymentForAdditionalClasses(PlayerSubscriptionEntity subscription, List<UUID> newClassTypeIds) {
        // Buscar el pago pendiente asociado a esta suscripción
        List<PaymentEntity> pendingPayments = paymentRepository.findBySubscription_Id(subscription.getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .collect(Collectors.toList());

        PaymentEntity payment;
        if (pendingPayments.isEmpty()) {
            // Crear nuevo pago si no existe
            payment = PaymentEntity.builder()
                    .player(subscription.getPlayer())
                    .subscription(subscription)
                    .status(PaymentStatus.PENDING)
                    .paymentDate(LocalDate.now())
                    .paymentType(PaymentType.QUARTERLY)
                    .quarterStartDate(subscription.getCurrentQuarterStart())
                    .quarterEndDate(subscription.getCurrentQuarterEnd())
                    .daysPerWeek(subscription.getDaysPerWeek())
                    .build();
        } else {
            // Usar el primer pago pendiente
            payment = pendingPayments.get(0);
        }

        // Calcular el monto adicional (140€ por cada nueva clase)
        BigDecimal additionalAmount = new BigDecimal("140").multiply(new BigDecimal(newClassTypeIds.size()));
        BigDecimal newTotalAmount = payment.getAmount() != null 
                ? payment.getAmount().add(additionalAmount)
                : additionalAmount;
        payment.setAmount(newTotalAmount);

        // Construir el concepto con los nombres de las clases añadidas
        StringBuilder conceptBuilder = new StringBuilder();
        if (payment.getNotes() != null && !payment.getNotes().isEmpty()) {
            conceptBuilder.append(payment.getNotes());
        } else {
            conceptBuilder.append("Trimestre ");
            if (subscription.getCurrentQuarterStart() != null) {
                int quarterNum = getQuarterNumber(subscription.getCurrentQuarterStart());
                conceptBuilder.append("Q").append(quarterNum).append(" ")
                        .append(subscription.getCurrentQuarterStart().getYear());
            }
        }

        // Añadir las nuevas clases al concepto
        for (UUID classTypeId : newClassTypeIds) {
            ClassTypeEntity classType = classTypeRepository.findById(classTypeId).orElse(null);
            if (classType != null) {
                String dayName = getDayNameInSpanish(classType.getDayOfWeek());
                String timeStr = classType.getStartTime().toString();
                if (conceptBuilder.length() > 0) {
                    conceptBuilder.append(" + ");
                }
                conceptBuilder.append(dayName).append(" ").append(timeStr);
            }
        }

        payment.setNotes(conceptBuilder.toString());
        payment.setDaysPerWeek(subscription.getDaysPerWeek());

        paymentRepository.save(payment);
    }

    private String getDayNameInSpanish(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}



