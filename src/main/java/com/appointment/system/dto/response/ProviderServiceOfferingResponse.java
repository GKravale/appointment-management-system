package com.appointment.system.dto.response;

import com.appointment.system.entity.ProviderServiceOffering;
import com.appointment.system.enums.BookingType;
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
    private Integer bufferMinutes = 0;
    private BookingType bookingType;

    public static ProviderServiceOfferingResponse from(ProviderServiceOffering providerServiceOffering) {
        ProviderServiceOfferingResponse offeringResponse = new ProviderServiceOfferingResponse();

        offeringResponse.setId(providerServiceOffering.getId());
        offeringResponse.setServiceOfferingId(providerServiceOffering.getServiceOffering().getId());
        offeringResponse.setTitle(providerServiceOffering.getServiceOffering().getTitle());
        offeringResponse.setDescription(providerServiceOffering.getServiceOffering().getDescription());
        offeringResponse.setCategory(providerServiceOffering.getServiceOffering().getCategory());
        offeringResponse.setEffectiveDuration(providerServiceOffering.getDurationOverride() != null ?
                providerServiceOffering.getDurationOverride() :
                providerServiceOffering.getServiceOffering().getDefaultDuration());
        offeringResponse.setEffectivePrice(providerServiceOffering.getPriceOverride() != null ?
                providerServiceOffering.getPriceOverride() :
                providerServiceOffering.getServiceOffering().getPriceEstimate());
        offeringResponse.setIsActive(providerServiceOffering.getIsActive());
        offeringResponse.setBufferMinutes(providerServiceOffering.getBufferMinutes() != null ?
                providerServiceOffering.getBufferMinutes() : 0);
        offeringResponse.setBookingType(providerServiceOffering.getServiceOffering().getBookingType());

        return offeringResponse;
    }
}