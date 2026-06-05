package com.appointment.system.dto.request;

import com.appointment.system.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).+$", message = "Password must contain at least one uppercase letter " +
            "and one number")
    private String password;

    @NotBlank
    @Size(min = 8, max = 100)
    private String confirmPassword;

    @NotBlank
    @Size(min = 3, max = 30)
    private String firstName;

    @NotBlank
    @Size(min = 3, max = 30)
    private String lastName;

    @Pattern(regexp = "^\\+?[\\d\\s\\-]{7,20}$", message = "Please enter a valid phone number (e.g. +371 22233233)")
    private String phoneNr;

    @NotNull
    private Role role;

    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTerms = false;
}