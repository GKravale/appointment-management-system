package com.appointment.system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "provider")
public class Provider extends Person {

    @Column(name = "location")
    private String location;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToOne(mappedBy = "provider", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Portfolio portfolio;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ProviderServiceOffering> serviceOfferings = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Availability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<TimeBlock> timeBlocks = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Post> posts = new ArrayList<>();

    public Provider(String firstName, String lastName, String phoneNr, String location, String bio) {
        super(firstName, lastName, phoneNr);
        this.location = location;
        this.bio = bio;
    }
}
