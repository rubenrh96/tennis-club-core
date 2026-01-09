package com.in4everyall.tennisclubmanager.tennisclub.entity;

import jakarta.persistence.*;
import lombok.*;
import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "players", uniqueConstraints = {@UniqueConstraint(name = "ux_players_month", columnNames = {"license_number", "phase_month"})})
public class PlayerEntity extends AuditableEntity {
    @Id
    @Column(name = "license_number", length = 50, nullable = false, updatable = false)
    private String licenseNumber;

    @Column(name = "forehand", length = 20)
    private String forehand;

    @Column(name = "backhand", length = 20)
    private String backhand;

    @Column(name = "group_no")
    private Integer groupNo;

    /**
     * Identificador funcional de fase, con formato "YYYY-F" (por ejemplo "2025-1").
     * Se almacena en la columna histórica {@code phase_month} para mantener compatibilidad de esquema.
     */
    @Column(name = "phase_month", nullable = false, length = 16)
    private String phaseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "license_number",
            referencedColumnName = "license_number",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_players_user")
    )
    private UserEntity user;

    @OneToMany(mappedBy = "player1", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<MatchEntity> matchesAsPlayer1 = new HashSet<>();

    @OneToMany(mappedBy = "player2", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<MatchEntity> matchesAsPlayer2 = new HashSet<>();

    @OneToMany(mappedBy = "winner", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<MatchEntity> matchesWon = new HashSet<>();
}
