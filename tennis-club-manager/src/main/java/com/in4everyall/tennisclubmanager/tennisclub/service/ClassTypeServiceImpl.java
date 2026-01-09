package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassTypeRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ClassTypeResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ClassTypeEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ClassTypeRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PlayerClassEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassTypeServiceImpl implements ClassTypeService {

    private final ClassTypeRepository classTypeRepository;
    private final PlayerClassEnrollmentRepository enrollmentRepository;

    @Override
    public ClassTypeResponse createClassType(ClassTypeRequest request) {
        ClassTypeEntity classType = ClassTypeEntity.builder()
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .name(request.name())
                .description(request.description())
                .maxCapacity(request.maxCapacity())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        ClassTypeEntity saved = classTypeRepository.save(classType);
        return toResponse(saved);
    }

    @Override
    public ClassTypeResponse getClassTypeById(UUID id) {
        ClassTypeEntity classType = classTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Tipo de clase no encontrado"));
        return toResponse(classType);
    }

    @Override
    public List<ClassTypeResponse> getAllClassTypes() {
        return classTypeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassTypeResponse> getActiveClassTypes() {
        return classTypeRepository.findAllActiveOrdered().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassTypeResponse> getClassTypesByDayOfWeek(DayOfWeek dayOfWeek) {
        return classTypeRepository.findByDayOfWeekActive(dayOfWeek).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClassTypeResponse updateClassType(UUID id, ClassTypeRequest request) {
        ClassTypeEntity classType = classTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Tipo de clase no encontrado"));

        classType.setDayOfWeek(request.dayOfWeek());
        classType.setStartTime(request.startTime());
        classType.setEndTime(request.endTime());
        classType.setName(request.name());
        classType.setDescription(request.description());
        classType.setMaxCapacity(request.maxCapacity());
        if (request.isActive() != null) {
            classType.setIsActive(request.isActive());
        }

        ClassTypeEntity updated = classTypeRepository.save(classType);
        return toResponse(updated);
    }

    @Override
    public void deleteClassType(UUID id) {
        if (!classTypeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de clase no encontrado");
        }
        classTypeRepository.deleteById(id);
    }

    private ClassTypeResponse toResponse(ClassTypeEntity classType) {
        // Contar jugadores inscritos activos en este tipo de clase
        // Por ahora contamos todos los enrollments activos para este tipo de clase
        Long enrolledCount = enrollmentRepository.findByClassType_Id(classType.getId())
                .stream()
                .filter(e -> e.getIsActive() != null && e.getIsActive())
                .count();

        return new ClassTypeResponse(
                classType.getId(),
                classType.getDayOfWeek(),
                classType.getStartTime(),
                classType.getEndTime(),
                classType.getName(),
                classType.getDescription(),
                classType.getMaxCapacity(),
                classType.getIsActive(),
                enrolledCount
        );
    }
}

