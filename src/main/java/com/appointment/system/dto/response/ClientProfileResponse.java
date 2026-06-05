package com.appointment.system.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ClientProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNr;
    private String notes;
    private String email;
}