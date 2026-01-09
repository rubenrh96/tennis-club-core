package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "class_types", indexes = {
    @Index(name = "ix_class_types_day", columnList = "day_of_week"),
    @Index(name = "ix_class_types_active", columnList = "is_active")
})
public class ClassTypeEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (isActive == null) isActive = true;
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, etc.
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // ej: 18:00
    
    @Column(name = "end_time")
    private LocalTime endTime; // ej: 19:30 (opcional)
    
    @Column(name = "name", length = 100)
    private String name; // opcional, ej: "Clase Avanzada Lunes"
    
    @Column(name = "description", length = 500)
    private String description; // opcional
    
    @Column(name = "max_capacity")
    private Integer maxCapacity; // opcional, capacidad máxima
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}

