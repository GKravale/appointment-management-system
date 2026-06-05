package com.appointment.system.dto.response;

import com.appointment.system.enums.ServiceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceOfferingResponse {
    private Long id;
    private String title;
    private String description;
    private int defaultDuration;
    private String priceEstimate;
    private ServiceCategory category;
    private Boolean isActive;
}