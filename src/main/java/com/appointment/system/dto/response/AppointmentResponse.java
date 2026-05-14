package com.appointment.system.dto.response;

import com.appointment.system.enums.AppointmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String clientFirstName;
    private String clientLastName;
    private String providerFirstName;
    private String providerLastName;
    private String serviceTitleSnapshot;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String clientNotes;
    private String providerNotes;
    private int durationAtBooking;
    private String priceAtBooking;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
}