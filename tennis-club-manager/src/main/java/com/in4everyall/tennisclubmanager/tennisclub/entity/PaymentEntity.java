package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "ix_payments_license", columnList = "license_number"),
    @Index(name = "ix_payments_date", columnList = "payment_date"),
    @Index(name = "ix_payments_status", columnList = "status")
})
public class PaymentEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = PaymentStatus.PENDING;
    }
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "license_number",
            referencedColumnName = "license_number",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_player")
    )
    private PlayerEntity player;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentType paymentType;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentStatus status;
    
    // Relación futura con calendario/clases
    @Column(name = "class_session_id", columnDefinition = "UUID")
    private UUID classSessionId;
    
    // Relación con suscripción (opcional, no todos los pagos son de suscripciones)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subscription_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_payment_subscription")
    )
    private PlayerSubscriptionEntity subscription;
    
    // Para clases individuales
    @Column(name = "class_date")
    private LocalDate classDate;
    
    // Para bonos
    @Column(name = "classes_remaining")
    private Integer classesRemaining;
    
    // Para trimestres
    @Column(name = "quarter_start_date")
    private LocalDate quarterStartDate;
    
    @Column(name = "quarter_end_date")
    private LocalDate quarterEndDate;
    
    @Column(name = "days_per_week")
    private Integer daysPerWeek;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "quarter_number")
    private Integer quarterNumber;
    
    @Column(name = "notes", length = 500)
    private String notes;
}


