package com.appointment.system.entity;

import com.appointment.system.enums.BookingType;
import com.appointment.system.enums.ServiceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "service_offering")
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_duration", nullable = false)
    private int defaultDuration;

    @Column(name = "price_estimate")
    private String priceEstimate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private ServiceCategory category;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "serviceOffering", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ProviderServiceOffering> providerServiceOfferings = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    private BookingType bookingType = BookingType.SLOT_BASED;

    public ServiceOffering(String title, String description, int defaultDuration, String priceEstimate,
                           ServiceCategory category) {
        this.title = title;
        this.description = description;
        this.defaultDuration = defaultDuration;
        this.priceEstimate = priceEstimate;
        this.category = category;
    }
}