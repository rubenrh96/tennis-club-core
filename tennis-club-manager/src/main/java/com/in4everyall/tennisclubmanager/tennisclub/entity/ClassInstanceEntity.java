package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.ClassInstanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "class_instances", indexes = {
    @Index(name = "ix_class_instances_date", columnList = "date"),
    @Index(name = "ix_class_instances_class_type", columnList = "class_type_id"),
    @Index(name = "ix_class_instances_quarter", columnList = "quarter_id"),
    @Index(name = "ix_class_instances_status", columnList = "status")
})
public class ClassInstanceEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = ClassInstanceStatus.SCHEDULED;
        if (isHoliday == null) isHoliday = false;
    }
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "class_type_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_class_instance_type")
    )
    private ClassTypeEntity classType;
    
    @Column(name = "date", nullable = false)
    private LocalDate date; // fecha específica de la clase
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "quarter_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_class_instance_quarter")
    )
    private QuarterEntity quarter;
    
    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday; // si es festivo
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ClassInstanceStatus status; // SCHEDULED, COMPLETED, CANCELLED
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason; // motivo de cancelación (si aplica)
}

