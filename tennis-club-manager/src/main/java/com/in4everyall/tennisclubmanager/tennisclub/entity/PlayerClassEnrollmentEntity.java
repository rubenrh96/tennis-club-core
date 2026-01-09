package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "player_class_enrollments", indexes = {
    @Index(name = "ix_enrollments_license", columnList = "license_number"),
    @Index(name = "ix_enrollments_class_type", columnList = "class_type_id"),
    @Index(name = "ix_enrollments_subscription", columnList = "subscription_id"),
    @Index(name = "ix_enrollments_quarter", columnList = "quarter_id")
})
public class PlayerClassEnrollmentEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (isActive == null) isActive = true;
    }
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "license_number",
            referencedColumnName = "license_number",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_enrollment_player")
    )
    private PlayerEntity player;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "class_type_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_enrollment_class_type")
    )
    private ClassTypeEntity classType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subscription_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_enrollment_subscription")
    )
    private PlayerSubscriptionEntity subscription; // Relación con suscripción
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "quarter_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_enrollment_quarter")
    )
    private QuarterEntity quarter;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}

