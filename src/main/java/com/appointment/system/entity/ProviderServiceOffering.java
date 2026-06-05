package com.appointment.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "provider_service_offering")
public class ProviderServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    @ToString.Exclude
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "service_offering_id", nullable = false)
    @ToString.Exclude
    private ServiceOffering serviceOffering;

    @Column(name = "duration_override")
    private Integer durationOverride;

    @Column(name = "price_override")
    private String priceOverride;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "buffer_minutes")
    private Integer bufferMinutes = 0;

    @OneToMany(mappedBy = "providerServiceOffering", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    public ProviderServiceOffering(Provider provider, ServiceOffering serviceOffering) {
        this.provider = provider;
        this.serviceOffering = serviceOffering;
    }

    public String getEffectivePrice() {
        return priceOverride != null ? priceOverride : serviceOffering.getPriceEstimate();
    }
}