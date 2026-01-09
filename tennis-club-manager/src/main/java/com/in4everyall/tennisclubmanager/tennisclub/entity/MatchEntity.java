package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "matches", indexes = {
    @Index(name = "ix_matches_month", columnList = "phase_month"),
    @Index(name = "ix_matches_p1", columnList = "player1_license"),
    @Index(name = "ix_matches_p2", columnList = "player2_license")})
public class MatchEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (cancelled == null) cancelled = false;
    }

    /**
     * Identificador funcional de fase, con formato "YYYY-F" (por ejemplo "2025-1").
     * Se almacena en la columna histórica {@code phase_month} para mantener compatibilidad de esquema.
     */
    @Column(name = "phase_month", nullable = false, length = 16)
    private String phaseCode;

    /**
     * Número de grupo del jugador en el momento de disputarse el partido.
     * Se utiliza para poder reconstruir clasificaciones históricas por fase y grupo.
     */
    @Column(name = "group_no_at_match")
    private Integer groupNoAtMatch;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "played_at")
    private Instant playedAt;

    // --- Participantes ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "player1_license",
            referencedColumnName = "license_number",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_match_p1")
    )
    private PlayerEntity player1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "player2_license",
            referencedColumnName = "license_number",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_match_p2")
    )
    private PlayerEntity player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "winner_license",
            referencedColumnName = "license_number",
            foreignKey = @ForeignKey(name = "fk_match_winner")
    )
    private PlayerEntity winner;

    @Column(name = "set1_p1")
    private Short set1P1;

    @Column(name = "set1_p2")
    private Short set1P2;

    @Column(name = "set2_p1")
    private Short set2P1;

    @Column(name = "set2_p2")
    private Short set2P2;

    @Column(name = "set3_p1")
    private Short set3P1;

    @Column(name = "set3_p2")
    private Short set3P2;

    @Column(name = "submitted_by_license")
    private String submittedByLicense;

    @Column(name = "confirmed", nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    @Column(name = "rejected", nullable = false)
    @Builder.Default
    private boolean rejected = false;

    @Column(name = "cancelled", nullable = false)
    @Builder.Default
    private Boolean cancelled = false;
}
