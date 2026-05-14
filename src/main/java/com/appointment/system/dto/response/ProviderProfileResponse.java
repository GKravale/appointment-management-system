package com.appointment.system.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProviderProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNr;
    private String location;
    private String bio;
    private Boolean isActive;
    private Integer cancellationHoursLimit;
    private List<ProviderServiceOfferingResponse> serviceOfferings;
}