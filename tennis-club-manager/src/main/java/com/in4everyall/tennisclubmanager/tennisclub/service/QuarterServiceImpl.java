package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.QuarterRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.QuarterResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.QuarterEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.QuarterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuarterServiceImpl implements QuarterService {

    private final QuarterRepository quarterRepository;

    @Override
    public QuarterResponse createQuarter(QuarterRequest request) {
        // Validar fechas
        if (request.startDate().isAfter(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }

        QuarterEntity quarter = QuarterEntity.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .isActive(request.isActive() != null ? request.isActive() : false)
                .build();

        QuarterEntity saved = quarterRepository.save(quarter);
        return toResponse(saved);
    }

    @Override
    public QuarterResponse getQuarterById(UUID id) {
        QuarterEntity quarter = quarterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Trimestre no encontrado"));
        return toResponse(quarter);
    }

    @Override
    public List<QuarterResponse> getAllQuarters() {
        return quarterRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuarterResponse getActiveQuarter() {
        return quarterRepository.findActiveQuarter()
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "No hay trimestre activo"));
    }

    @Override
    public QuarterResponse getQuarterByDate(LocalDate date) {
        return quarterRepository.findByDate(date)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "No se encontró trimestre para la fecha: " + date));
    }

    @Override
    public QuarterResponse updateQuarter(UUID id, QuarterRequest request) {
        QuarterEntity quarter = quarterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Trimestre no encontrado"));

        if (request.startDate().isAfter(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }

        quarter.setName(request.name());
        quarter.setStartDate(request.startDate());
        quarter.setEndDate(request.endDate());
        if (request.isActive() != null) {
            quarter.setIsActive(request.isActive());
        }

        QuarterEntity updated = quarterRepository.save(quarter);
        return toResponse(updated);
    }

    @Override
    public void deleteQuarter(UUID id) {
        if (!quarterRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trimestre no encontrado");
        }
        quarterRepository.deleteById(id);
    }

    private QuarterResponse toResponse(QuarterEntity quarter) {
        return new QuarterResponse(
                quarter.getId(),
                quarter.getName(),
                quarter.getStartDate(),
                quarter.getEndDate(),
                quarter.getIsActive()
        );
    }
}

