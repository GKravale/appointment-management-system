package com.appointment.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.JOINED)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 30)
    @Pattern(regexp = "[A-ZĒŪĪĻĶĢŠĀČŅŽ][a-zēūīļķģšāžčņ ]+([A-ZĒŪĪĻĶĢŠĀČŅŽ][a-zēūīļķģšāžčņ ]+)*")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull
    @Size(min = 3, max = 30)
    @Pattern(regexp = "[A-ZĒŪĪĻĶĢŠĀČŅŽ][a-zēūīļķģšāžčņ ]+([A-ZĒŪĪĻĶĢŠĀČŅŽ][a-zēūīļķģšāžčņ ]+)*")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Pattern(regexp = "^\\+?\\d{7,15}$")
    @Column(name = "phone_nr")
    private String phoneNr;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Person(String firstName, String lastName, String phoneNr) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNr = phoneNr;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
