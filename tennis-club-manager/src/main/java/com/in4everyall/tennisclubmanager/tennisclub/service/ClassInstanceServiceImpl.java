package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.entity.*;
import com.in4everyall.tennisclubmanager.tennisclub.enums.ClassInstanceStatus;
import com.in4everyall.tennisclubmanager.tennisclub.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassInstanceServiceImpl implements ClassInstanceService {

    private final ClassInstanceRepository classInstanceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final QuarterRepository quarterRepository;
    private final HolidayRepository holidayRepository;
    private final PlayerClassEnrollmentRepository enrollmentRepository;
    private final ClassConsumptionRepository consumptionRepository;

    @Override
    @Transactional
    public List<ClassInstanceResponse> generateInstancesForSubscription(
            UUID subscriptionId,
            List<UUID> classTypeIds,
            LocalDate quarterStart,
            LocalDate quarterEnd
    ) {
        List<ClassInstanceEntity> instances = new ArrayList<>();
        
        // Obtener o crear el trimestre
        QuarterEntity quarter = quarterRepository.findByDateRange(quarterStart, quarterEnd)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    QuarterEntity newQuarter = QuarterEntity.builder()
                            .name("Q" + getQuarterNumber(quarterStart) + " " + quarterStart.getYear())
                            .startDate(quarterStart)
                            .endDate(quarterEnd)
                            .isActive(true)
                            .build();
                    return quarterRepository.save(newQuarter);
                });

        for (UUID classTypeId : classTypeIds) {
            ClassTypeEntity classType = classTypeRepository.findById(classTypeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Tipo de clase no encontrado: " + classTypeId));

            LocalDate currentDate = quarterStart;
            while (!currentDate.isAfter(quarterEnd)) {
                // Excluir domingos siempre
                if (currentDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    currentDate = currentDate.plusDays(1);
                    continue;
                }
                
                // Verificar si es el día de la semana correcto
                if (currentDate.getDayOfWeek() == classType.getDayOfWeek()) {
                    // Verificar si es festivo
                    boolean isHoliday = holidayRepository.existsByDate(currentDate);

                    ClassInstanceEntity instance = ClassInstanceEntity.builder()
                            .classType(classType)
                            .date(currentDate)
                            .quarter(quarter)
                            .isHoliday(isHoliday)
                            .status(isHoliday ? ClassInstanceStatus.CANCELLED : ClassInstanceStatus.SCHEDULED)
                            .cancellationReason(isHoliday ? "Festivo" : null)
                            .build();

                    instances.add(instance);
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        List<ClassInstanceEntity> savedInstances = classInstanceRepository.saveAll(instances);
        return savedInstances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClassInstanceResponse getClassInstanceById(UUID id) {
        ClassInstanceEntity instance = classInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Instancia de clase no encontrada"));
        return toResponse(instance);
    }

    @Override
    public List<ClassInstanceResponse> getClassInstancesByQuarter(UUID quarterId) {
        return classInstanceRepository.findByQuarter_Id(quarterId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassInstanceResponse> getClassInstancesByClassType(UUID classTypeId) {
        return classInstanceRepository.findByClassType_Id(classTypeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CalendarResponse getCalendarForPlayer(String licenseNumber, LocalDate startDate, LocalDate endDate) {
        // Obtener inscripciones activas del jugador
        List<PlayerClassEnrollmentEntity> enrollments = enrollmentRepository
                .findByPlayer_LicenseNumberAndIsActive(licenseNumber, true);

        // Obtener instancias de clase para las fechas y tipos de clase del jugador
        List<UUID> classTypeIds = enrollments.stream()
                .map(e -> e.getClassType().getId())
                .distinct()
                .collect(Collectors.toList());

        List<ClassInstanceEntity> instances = classInstanceRepository
                .findByDateRange(startDate, endDate).stream()
                .filter(ci -> classTypeIds.contains(ci.getClassType().getId()))
                .collect(Collectors.toList());

        // Obtener festivos en el rango
        List<HolidayEntity> holidays = holidayRepository.findByDateRange(startDate, endDate);

        // Obtener consumos de bonos del jugador en el rango de fechas para marcar clases consumidas
        List<ClassConsumptionEntity> consumptions = consumptionRepository
                .findByPlayerOrdered(licenseNumber).stream()
                .filter(c -> !c.getClassDate().isBefore(startDate) && !c.getClassDate().isAfter(endDate))
                .collect(Collectors.toList());

        return buildCalendarResponse(instances, holidays, startDate, endDate, false, licenseNumber, consumptions);
    }

    @Override
    public CalendarResponse getCalendarForAdmin(LocalDate startDate, LocalDate endDate, UUID quarterId, UUID classTypeId) {
        List<ClassInstanceEntity> instances;
        
        if (quarterId != null && classTypeId != null) {
            instances = classInstanceRepository.findByQuarterAndClassType(quarterId, classTypeId);
        } else if (quarterId != null) {
            instances = classInstanceRepository.findByQuarter_Id(quarterId);
        } else if (classTypeId != null) {
            instances = classInstanceRepository.findByClassTypeAndDateRange(classTypeId, startDate, endDate);
        } else {
            instances = classInstanceRepository.findByDateRange(startDate, endDate);
        }

        List<HolidayEntity> holidays = holidayRepository.findByDateRange(startDate, endDate);

        return buildCalendarResponse(instances, holidays, startDate, endDate, true, null, List.of());
    }

    @Override
    @Transactional
    public ClassInstanceResponse cancelClassInstance(UUID id, String reason) {
        ClassInstanceEntity instance = classInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Instancia de clase no encontrada"));

        instance.setStatus(ClassInstanceStatus.CANCELLED);
        instance.setCancellationReason(reason);

        ClassInstanceEntity updated = classInstanceRepository.save(instance);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public ClassInstanceResponse completeClassInstance(UUID id) {
        ClassInstanceEntity instance = classInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Instancia de clase no encontrada"));

        instance.setStatus(ClassInstanceStatus.COMPLETED);

        ClassInstanceEntity updated = classInstanceRepository.save(instance);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteClassInstance(UUID id) {
        if (!classInstanceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instancia de clase no encontrada");
        }
        classInstanceRepository.deleteById(id);
    }

    private ClassInstanceResponse toResponse(ClassInstanceEntity instance) {
        String classTypeName = instance.getClassType().getDayOfWeek().name() + " " + 
                              instance.getClassType().getStartTime().toString();
        
        // Obtener jugadores inscritos (para admin)
        List<String> playerNames = new ArrayList<>();
        if (instance.getClassType() != null) {
            List<PlayerClassEnrollmentEntity> enrollments = enrollmentRepository
                    .findByClassTypeAndQuarter(instance.getClassType().getId(), instance.getQuarter().getId());
            playerNames = enrollments.stream()
                    .filter(e -> e.getIsActive())
                    .map(e -> e.getPlayer().getUser() != null ? 
                            e.getPlayer().getUser().getFirstName() + " " + e.getPlayer().getUser().getLastName() : 
                            e.getPlayer().getLicenseNumber())
                    .collect(Collectors.toList());
        }

        return new ClassInstanceResponse(
                instance.getId(),
                instance.getClassType().getId(),
                classTypeName,
                instance.getDate(),
                instance.getQuarter().getId(),
                instance.getQuarter().getName(),
                instance.getIsHoliday(),
                instance.getStatus(),
                instance.getCancellationReason(),
                playerNames
        );
    }

    private CalendarResponse buildCalendarResponse(
            List<ClassInstanceEntity> instances,
            List<HolidayEntity> holidays,
            LocalDate startDate,
            LocalDate endDate,
            boolean isAdmin,
            String playerLicenseNumber,
            List<ClassConsumptionEntity> consumptions
    ) {
        // Crear un mapa de consumos para búsqueda rápida: clave = fecha + hora + classTypeId
        Map<String, ClassConsumptionEntity> consumptionsMap = new HashMap<>();
        if (consumptions != null && playerLicenseNumber != null) {
            for (ClassConsumptionEntity consumption : consumptions) {
                String key = consumption.getClassDate().toString() + "_" +
                            (consumption.getClassTime() != null ? consumption.getClassTime().toString() : "") + "_" +
                            (consumption.getClassType() != null ? consumption.getClassType().getId().toString() : "");
                consumptionsMap.put(key, consumption);
            }
        }
        Map<LocalDate, List<ClassInstanceEntity>> instancesByDate = instances.stream()
                .collect(Collectors.groupingBy(ClassInstanceEntity::getDate));

        Map<LocalDate, HolidayEntity> holidaysByDate = holidays.stream()
                .collect(Collectors.toMap(HolidayEntity::getDate, h -> h));

        List<CalendarDayResponse> days = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            List<ClassInstanceEntity> dayInstances = instancesByDate.getOrDefault(currentDate, new ArrayList<>());
            HolidayEntity holiday = holidaysByDate.get(currentDate);

            // Agrupar instancias por classType + startTime para evitar duplicados
            Map<String, List<ClassInstanceEntity>> instancesByClassType = dayInstances.stream()
                    .collect(Collectors.groupingBy(ci -> 
                            ci.getClassType().getId().toString() + "_" + ci.getClassType().getStartTime().toString()));

            List<ClassInstanceInfoResponse> classInfos = new ArrayList<>();
            for (List<ClassInstanceEntity> groupedInstances : instancesByClassType.values()) {
                // Tomar la primera instancia como representante
                ClassInstanceEntity representative = groupedInstances.get(0);
                String classTypeName = getDayNameInSpanish(representative.getClassType().getDayOfWeek()) + " " + 
                                      representative.getClassType().getStartTime().toString();
                
                List<String> playerNames = new ArrayList<>();
                if (isAdmin) {
                    // Para admin: obtener todos los jugadores inscritos en este tipo de clase y trimestre
                    List<PlayerClassEnrollmentEntity> enrollments = enrollmentRepository
                            .findByClassTypeAndQuarter(representative.getClassType().getId(), representative.getQuarter().getId());
                    playerNames = enrollments.stream()
                            .filter(e -> e.getIsActive())
                            .map(e -> e.getPlayer().getUser() != null ? 
                                    e.getPlayer().getUser().getFirstName() + " " + e.getPlayer().getUser().getLastName() : 
                                    e.getPlayer().getLicenseNumber())
                            .distinct()
                            .collect(Collectors.toList());
                }
                // Para jugador: playerNames queda vacío (no se muestran otros jugadores)

                // Determinar el estado: si alguna instancia está cancelada, la clase está cancelada
                ClassInstanceStatus status = groupedInstances.stream()
                        .anyMatch(ci -> ci.getStatus() == ClassInstanceStatus.CANCELLED) 
                        ? ClassInstanceStatus.CANCELLED 
                        : representative.getStatus();

                // Obtener información de capacidad para determinar si es individual o grupal
                Integer maxCapacity = representative.getClassType().getMaxCapacity();
                // Si maxCapacity es null o 1, es clase individual; si es >1, es grupal
                Boolean isIndividual = (maxCapacity == null || maxCapacity == 1);

                // Verificar si esta clase fue consumida con un bono (solo para jugadores)
                Boolean isConsumed = false;
                if (!isAdmin && playerLicenseNumber != null && !consumptionsMap.isEmpty()) {
                    String consumptionKey = representative.getDate().toString() + "_" +
                                           representative.getClassType().getStartTime().toString() + "_" +
                                           representative.getClassType().getId().toString();
                    isConsumed = consumptionsMap.containsKey(consumptionKey);
                }

                classInfos.add(new ClassInstanceInfoResponse(
                        representative.getId(),
                        classTypeName,
                        representative.getClassType().getStartTime(),
                        representative.getClassType().getEndTime(),
                        status,
                        playerNames,
                        isIndividual,
                        maxCapacity,
                        isConsumed
                ));
            }

            // Ordenar por hora de inicio
            classInfos.sort((a, b) -> a.startTime().compareTo(b.startTime()));

            boolean isCancelled = dayInstances.stream()
                    .anyMatch(ci -> ci.getStatus() == ClassInstanceStatus.CANCELLED && !ci.getIsHoliday());
            
            String cancellationReason = dayInstances.stream()
                    .filter(ci -> ci.getStatus() == ClassInstanceStatus.CANCELLED && !ci.getIsHoliday())
                    .findFirst()
                    .map(ClassInstanceEntity::getCancellationReason)
                    .orElse(null);

            days.add(new CalendarDayResponse(
                    currentDate,
                    classInfos,
                    holiday != null || (dayInstances.stream().anyMatch(ClassInstanceEntity::getIsHoliday)),
                    holiday != null ? holiday.getName() : null,
                    isCancelled,
                    cancellationReason
            ));

            currentDate = currentDate.plusDays(1);
        }

        return new CalendarResponse(startDate, endDate, days);
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

    private int getQuarterNumber(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 1 && month <= 3) return 1;
        if (month >= 4 && month <= 6) return 2;
        if (month >= 9 && month <= 12) return 3;
        return 1; // Default
    }
}

