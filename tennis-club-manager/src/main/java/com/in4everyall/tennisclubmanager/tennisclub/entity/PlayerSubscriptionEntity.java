package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "player_subscriptions", indexes = {
    @Index(name = "ix_subscriptions_license", columnList = "license_number"),
    @Index(name = "ix_subscriptions_active", columnList = "is_active")
})
public class PlayerSubscriptionEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (isActive == null) isActive = true;
        if (autoRenew == null) autoRenew = false;
    }
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "license_number",
            referencedColumnName = "license_number",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscription_player")
    )
    private PlayerEntity player;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SubscriptionType subscriptionType;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    // Para bonos
    @Column(name = "classes_remaining")
    private Integer classesRemaining;
    
    @Column(name = "package_purchase_date")
    private LocalDate packagePurchaseDate;
    
    // Para trimestres
    @Column(name = "days_per_week")
    private Integer daysPerWeek;
    
    @Column(name = "current_quarter_start")
    private LocalDate currentQuarterStart;
    
    @Column(name = "current_quarter_end")
    private LocalDate currentQuarterEnd;
    
    @Column(name = "auto_renew")
    private Boolean autoRenew;
    
    // Relación con pagos asociados a esta suscripción
    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity> payments = new HashSet<>();
    
    // Relación con inscripciones a tipos de clases
    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerClassEnrollmentEntity> classEnrollments = new HashSet<>();
    
    // Relación con consumos de clases (para bonos)
    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<com.in4everyall.tennisclubmanager.tennisclub.entity.ClassConsumptionEntity> classConsumptions = new HashSet<>();
    
    /**
     * Método helper para obtener número de clases restantes (para bonos)
     */
    public Integer getClassesRemaining() {
        if (subscriptionType != SubscriptionType.CLASS_PACKAGE) {
            return null;
        }
        int total = classesRemaining != null ? classesRemaining : 10;
        int consumed = classConsumptions != null ? classConsumptions.size() : 0;
        return Math.max(0, total - consumed);
    }
}


