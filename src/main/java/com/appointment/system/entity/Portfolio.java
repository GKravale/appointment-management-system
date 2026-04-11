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
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "provider_id", nullable = false, unique = true)
    @ToString.Exclude
    private Provider provider;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MediaAsset> mediaAssets = new ArrayList<>();
}