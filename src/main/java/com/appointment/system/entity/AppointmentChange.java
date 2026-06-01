package com.appointment.system.entity;

import com.appointment.system.enums.ChangeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "appointment_change")
public class AppointmentChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    @ToString.Exclude
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    @ToString.Exclude
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChangeType type;

    @Column(name = "old_start")
    private LocalDateTime oldStart;

    @Column(name = "new_start")
    private LocalDateTime newStart;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    public AppointmentChange(Appointment appointment, User changedBy, ChangeType type, LocalDateTime oldStart, LocalDateTime newStart, String reason) {
        this.appointment = appointment;
        this.changedBy = changedBy;
        this.type = type;
        this.oldStart = oldStart;
        this.newStart = newStart;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
