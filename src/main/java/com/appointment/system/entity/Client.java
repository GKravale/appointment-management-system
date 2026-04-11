package com.appointment.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "client")
public class Client extends Person {

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    public Client(String firstName, String lastName, String phoneNr) {
        super(firstName, lastName, phoneNr);
    }
}
