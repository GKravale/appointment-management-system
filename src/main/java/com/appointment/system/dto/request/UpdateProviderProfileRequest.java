package com.appointment.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProviderProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 30, message = "First name must be between 3 and 30 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 30, message = "Last name must be between 3 and 30 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[\\d\\s\\-]{7,20}$",
            message = "Please enter a valid phone number (e.g. +371 22233233)")
    private String phoneNr;

    private String location;

    private String bio;

    private Integer cancellationHoursLimit = 24;
}