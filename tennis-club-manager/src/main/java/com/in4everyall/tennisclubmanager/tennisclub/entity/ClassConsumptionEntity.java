package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "class_consumptions", indexes = {
    @Index(name = "ix_consumptions_license", columnList = "license_number"),
    @Index(name = "ix_consumptions_subscription", columnList = "subscription_id"),
    @Index(name = "ix_consumptions_date", columnList = "class_date")
})
public class ClassConsumptionEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (consumedBy == null) consumedBy = "ADMIN";
    }
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "license_number",
            referencedColumnName = "license_number",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_consumption_player")
    )
    private PlayerEntity player;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subscription_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_consumption_subscription")
    )
    private PlayerSubscriptionEntity subscription; // Bono de clases
    
    @Column(name = "class_date", nullable = false)
    private LocalDate classDate; // Fecha de la clase consumida
    
    @Column(name = "class_time")
    private LocalTime classTime; // Hora de la clase consumida
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "class_type_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_consumption_class_type")
    )
    private ClassTypeEntity classType; // Tipo de clase (opcional)
    
    @Column(name = "consumed_by", nullable = false, length = 50)
    private String consumedBy; // "ADMIN" o "SYSTEM"
}

