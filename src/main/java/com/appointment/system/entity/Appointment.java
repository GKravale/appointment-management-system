package com.appointment.system.entity;

import com.appointment.system.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    @ToString.Exclude
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "provider_service_offering_id", nullable = false)
    @ToString.Exclude
    private ProviderServiceOffering providerServiceOffering;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "client_notes", columnDefinition = "TEXT")
    private String clientNotes;

    @Column(name = "provider_notes", columnDefinition = "TEXT")
    private String providerNotes;

    @Column(name = "duration_at_booking", nullable = false)
    private int durationAtBooking;

    @Column(name = "price_at_booking")
    private String priceAtBooking;

    @Column(name = "service_title_snapshot")
    private String serviceTitleSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<AppointmentChange> changes = new ArrayList<>();

    public Appointment(Client client, Provider provider, ProviderServiceOffering providerServiceOffering,
                       LocalDateTime startTime, LocalDateTime endTime,
                       int durationAtBooking, String priceAtBooking, String serviceTitleSnapshot) {
        this.client = client;
        this.provider = provider;
        this.providerServiceOffering = providerServiceOffering;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AppointmentStatus.REQUESTED;
        this.durationAtBooking = durationAtBooking;
        this.priceAtBooking = priceAtBooking;
        this.serviceTitleSnapshot = serviceTitleSnapshot;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
