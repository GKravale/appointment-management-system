package com.appointment.system.service;

import com.appointment.system.dto.request.CreateServiceOfferingRequest;
import com.appointment.system.dto.response.ProviderServiceOfferingResponse;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.ProviderServiceOffering;
import com.appointment.system.entity.ServiceOffering;
import com.appointment.system.enums.BookingType;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import com.appointment.system.repository.ServiceOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProviderServiceOfferingRepository providerServiceOfferingRepository;
    private final ProviderRepository providerRepository;

    public List<ProviderServiceOfferingResponse> getProviderOfferings(Long personId) {
        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        return providerServiceOfferingRepository
                .findByProviderAndIsActiveTrueAndIsDeletedFalse(provider)
                .stream()
                .map(ProviderServiceOfferingResponse::from)
                .toList();
    }

    public List<ServiceOffering> getAllBaseOfferings() {
        return serviceOfferingRepository.findByIsActiveTrueAndIsDeletedFalse();
    }

    public List<ServiceCategory> getAllCategories() {
        return List.of(ServiceCategory.values());
    }

    @Transactional
    public void createAndAssign(Long personId, CreateServiceOfferingRequest request) {
        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        ServiceOffering offering = new ServiceOffering(
                request.getTitle(),
                request.getDescription(),
                request.getDefaultDuration(),
                request.getPriceEstimate(),
                request.getCategory()
        );
        offering.setBookingType(request.getBookingType() != null ? request.getBookingType() : BookingType.SLOT_BASED);
        serviceOfferingRepository.save(offering);

        ProviderServiceOffering providerOffering = new ProviderServiceOffering(provider, offering);
        providerOffering.setBufferMinutes(request.getBufferMinutes());
        providerServiceOfferingRepository.save(providerOffering);
    }

    @Transactional
    public void update(Long providerServiceOfferingId, Long personId, CreateServiceOfferingRequest request) {
        ProviderServiceOffering offering = providerServiceOfferingRepository.findById(providerServiceOfferingId)
                        .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        if (!offering.getProvider().getId().equals(personId)) {
            throw new SecurityException("Not authorized");
        }
        offering.getServiceOffering().setTitle(request.getTitle());
        offering.getServiceOffering().setDescription(request.getDescription());
        offering.getServiceOffering().setDefaultDuration(request.getDefaultDuration());
        offering.getServiceOffering().setPriceEstimate(request.getPriceEstimate());
        offering.getServiceOffering().setCategory(request.getCategory());
        offering.getServiceOffering().setBookingType(request.getBookingType() != null ? request.getBookingType() :
                BookingType.SLOT_BASED);
        offering.setBufferMinutes(request.getBufferMinutes());
    }

    @Transactional
    public void deactivate(Long providerServiceOfferingId, Long personId) {
        ProviderServiceOffering offering = providerServiceOfferingRepository
                .findById(providerServiceOfferingId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        if (!offering.getProvider().getId().equals(personId)) {
            throw new SecurityException("Not authorized");
        }

        offering.setIsActive(false);
    }
}