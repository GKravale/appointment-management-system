package com.appointment.system.entity;

import com.appointment.system.enums.BlockType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "time_block")
public class TimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    @ToString.Exclude
    private Provider provider;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BlockType type;

    @Column(name = "note")
    private String note;

    public TimeBlock(Provider provider, LocalDateTime startDateTime, LocalDateTime endDateTime, BlockType type,
                     String note) {
        this.provider = provider;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.type = type;
        this.note = note;
    }
}
