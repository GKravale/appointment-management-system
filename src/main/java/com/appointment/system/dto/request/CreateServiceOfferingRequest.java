package com.appointment.system.dto.request;

import com.appointment.system.enums.ServiceCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateServiceOfferingRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    private String description;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 480, message = "Duration cannot exceed 8 hours")
    private int defaultDuration;

    private String priceEstimate;

    @NotNull(message = "Please select a category")
    private ServiceCategory category;
}