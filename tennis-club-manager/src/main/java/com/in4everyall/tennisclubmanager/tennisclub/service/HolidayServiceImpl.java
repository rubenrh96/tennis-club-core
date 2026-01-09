package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.HolidayRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.HolidayResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.HolidayEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.HolidayRepository;
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
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    @Override
    public HolidayResponse createHoliday(HolidayRequest request) {
        // Verificar si ya existe un festivo para esa fecha
        if (holidayRepository.existsByDate(request.date())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Ya existe un festivo para la fecha: " + request.date());
        }

        HolidayEntity holiday = HolidayEntity.builder()
                .date(request.date())
                .name(request.name())
                .region(request.region() != null ? request.region() : "Castilla y León")
                .isNational(request.isNational() != null ? request.isNational() : true)
                .year(request.date().getYear())
                .build();

        HolidayEntity saved = holidayRepository.save(holiday);
        return toResponse(saved);
    }

    @Override
    public HolidayResponse getHolidayById(UUID id) {
        HolidayEntity holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Festivo no encontrado"));
        return toResponse(holiday);
    }

    @Override
    public List<HolidayResponse> getAllHolidays() {
        return holidayRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayResponse> getHolidaysByYear(Integer year) {
        return holidayRepository.findByYear(year).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayResponse> getHolidaysByRegion(String region) {
        return holidayRepository.findByRegion(region).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByDateRange(startDate, endDate).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }

    @Override
    public void syncHolidaysFromAPI(Integer year) {
        // TODO: Implementar sincronización con API externa
        // Por ahora, se pueden crear manualmente
        // API sugerida: https://date.nager.at/api/v3/PublicHolidays/{year}/ES
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, 
                "Sincronización con API externa pendiente de implementar");
    }

    @Override
    public void deleteHoliday(UUID id) {
        if (!holidayRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Festivo no encontrado");
        }
        holidayRepository.deleteById(id);
    }

    private HolidayResponse toResponse(HolidayEntity holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getDate(),
                holiday.getName(),
                holiday.getRegion(),
                holiday.getIsNational(),
                holiday.getYear()
        );
    }
}

