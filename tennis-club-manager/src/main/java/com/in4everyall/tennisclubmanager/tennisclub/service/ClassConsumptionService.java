package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassConsumptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassConsumptionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.entity.*;
import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import com.in4everyall.tennisclubmanager.tennisclub.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassConsumptionService {

    private final ClassConsumptionRepository consumptionRepository;
    private final PlayerSubscriptionRepository subscriptionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public ClassConsumptionResponse consumeClass(ClassConsumptionRequest request) {
        PlayerSubscriptionEntity subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Suscripción no encontrada"));

        if (subscription.getSubscriptionType() != SubscriptionType.CLASS_PACKAGE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La suscripción no es un bono de clases");
        }

        // Verificar clases restantes
        int remaining = subscription.getClassesRemaining();
        if (remaining <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "No quedan clases disponibles en el bono");
        }

        // Obtener tipo de clase si se proporciona
        ClassTypeEntity classType = null;
        if (request.classTypeId() != null) {
            classType = classTypeRepository.findById(request.classTypeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Tipo de clase no encontrado"));
        }

        // Crear consumo
        ClassConsumptionEntity consumption = ClassConsumptionEntity.builder()
                .player(subscription.getPlayer())
                .subscription(subscription)
                .classDate(request.classDate())
                .classTime(request.classTime())
                .classType(classType)
                .consumedBy("ADMIN")
                .build();

        consumption = consumptionRepository.save(consumption);

        // Calcular clases restantes después del consumo
        int newRemaining = subscription.getClassesRemaining();

        return toResponse(consumption, newRemaining);
    }

    public List<ClassConsumptionResponse> getConsumptionsBySubscription(UUID subscriptionId) {
        // Validar que la suscripción existe
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Suscripción no encontrada: " + subscriptionId);
        }

        try {
            List<ClassConsumptionEntity> consumptions = consumptionRepository
                    .findBySubscriptionOrdered(subscriptionId);
            
            // Si no hay consumos, devolver lista vacía (no error)
            if (consumptions == null || consumptions.isEmpty()) {
                return List.of();
            }
            
            return consumptions.stream()
                    .map(c -> {
                        try {
                            return toResponse(c, null);
                        } catch (Exception e) {
                            // Log del error pero continuar con los demás
                            System.err.println("Error al mapear consumo " + c.getId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(c -> c != null) // Filtrar nulos en caso de error
                    .collect(Collectors.toList());
        } catch (ResponseStatusException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            // Manejar cualquier otro error interno
            System.err.println("Error al obtener consumos para suscripción " + subscriptionId + ": " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al obtener consumos de la suscripción: " + e.getMessage());
        }
    }

    public List<ClassConsumptionResponse> getConsumptionsByPlayer(String licenseNumber) {
        List<ClassConsumptionEntity> consumptions = consumptionRepository
                .findByPlayerOrdered(licenseNumber);
        return consumptions.stream()
                .map(c -> toResponse(c, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteConsumption(UUID id) {
        if (!consumptionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumo no encontrado");
        }
        consumptionRepository.deleteById(id);
    }

    @Transactional
    public PlayerSubscriptionEntity renewBonoAutomatically(UUID subscriptionId) {
        PlayerSubscriptionEntity oldSubscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Suscripción no encontrada"));

        if (oldSubscription.getSubscriptionType() != SubscriptionType.CLASS_PACKAGE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La suscripción no es un bono de clases");
        }

        // Crear nuevo bono de 10 clases usando el servicio de suscripciones
        SubscriptionRequest newBonoRequest =
                new SubscriptionRequest(
                        oldSubscription.getPlayer().getLicenseNumber(),
                        SubscriptionType.CLASS_PACKAGE,
                        10, // clases restantes
                        java.time.LocalDate.now(), // fecha de compra
                        null, // daysPerWeek
                        null, // currentQuarterStart
                        null, // currentQuarterEnd
                        null, // classTypeIds (null para bonos)
                        null // autoRenew
                );

        com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse newSubscriptionResponse = 
                subscriptionService.createSubscription(newBonoRequest);

        // Obtener la entidad guardada
        return subscriptionRepository.findById(newSubscriptionResponse.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Error al crear nuevo bono"));
    }

    private ClassConsumptionResponse toResponse(ClassConsumptionEntity consumption, Integer classesRemaining) {
        // Validar que las relaciones necesarias existan
        if (consumption == null) {
            throw new IllegalArgumentException("Consumo no puede ser nulo");
        }
        
        if (consumption.getPlayer() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "El consumo no tiene jugador asociado");
        }
        
        if (consumption.getSubscription() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "El consumo no tiene suscripción asociada");
        }

        // Obtener nombre del jugador de forma segura
        String playerName;
        try {
            if (consumption.getPlayer().getUser() != null) {
                String firstName = consumption.getPlayer().getUser().getFirstName();
                String lastName = consumption.getPlayer().getUser().getLastName();
                playerName = (firstName != null ? firstName : "") + " " + 
                            (lastName != null ? lastName : "");
                playerName = playerName.trim();
                if (playerName.isEmpty()) {
                    playerName = consumption.getPlayer().getLicenseNumber();
                }
            } else {
                playerName = consumption.getPlayer().getLicenseNumber();
            }
        } catch (Exception e) {
            // Si hay error al obtener el nombre, usar la licencia
            playerName = consumption.getPlayer().getLicenseNumber();
        }

        // Obtener nombre del tipo de clase de forma segura
        String classTypeName = null;
        UUID classTypeId = null;
        try {
            if (consumption.getClassType() != null) {
                classTypeId = consumption.getClassType().getId();
                String dayName = consumption.getClassType().getDayOfWeek() != null ?
                        consumption.getClassType().getDayOfWeek().name() : "";
                String timeStr = consumption.getClassType().getStartTime() != null ?
                        consumption.getClassType().getStartTime().toString() : "";
                if (!dayName.isEmpty() && !timeStr.isEmpty()) {
                    classTypeName = dayName + " " + timeStr;
                }
            }
        } catch (Exception e) {
            // Si hay error, classTypeName queda null
            System.err.println("Error al obtener información del tipo de clase: " + e.getMessage());
        }

        return new ClassConsumptionResponse(
                consumption.getId(),
                consumption.getPlayer().getLicenseNumber(),
                playerName,
                consumption.getSubscription().getId(),
                consumption.getClassDate(),
                consumption.getClassTime(),
                classTypeId,
                classTypeName,
                consumption.getConsumedBy(),
                classesRemaining
        );
    }
}

