package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "holidays", indexes = {
    @Index(name = "ix_holidays_date", columnList = "date"),
    @Index(name = "ix_holidays_year", columnList = "year"),
    @Index(name = "ix_holidays_region", columnList = "region")
})
public class HolidayEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (isNational == null) isNational = true;
        if (year == null && date != null) {
            year = date.getYear();
        }
    }
    
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name; // ej: "Día de la Comunidad"
    
    @Column(name = "region", length = 100)
    private String region; // ej: "Castilla y León"
    
    @Column(name = "is_national", nullable = false)
    private Boolean isNational;
    
    @Column(name = "year")
    private Integer year; // para facilitar búsquedas
}

