package com.appointment.system.dto.response;

import com.appointment.system.enums.ServiceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProviderServiceOfferingResponse {
    private Long id;
    private Long serviceOfferingId;
    private String title;
    private String description;
    private ServiceCategory category;
    private int effectiveDuration;
    private String effectivePrice;
    private Boolean isActive;
}