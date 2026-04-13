package com.appointment.system.service;

import com.appointment.system.dto.request.CreateServiceOfferingRequest;
import com.appointment.system.dto.response.ProviderServiceOfferingResponse;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.ProviderServiceOffering;
import com.appointment.system.entity.ServiceOffering;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import com.appointment.system.repository.ServiceOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
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
                .map(this::toResponse)
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
        serviceOfferingRepository.save(offering);

        ProviderServiceOffering providerOffering = new ProviderServiceOffering(provider, offering);
        providerServiceOfferingRepository.save(providerOffering);
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

    private ProviderServiceOfferingResponse toResponse(ProviderServiceOffering providerServiceOffering) {
        ProviderServiceOfferingResponse response = new ProviderServiceOfferingResponse();
        response.setId(providerServiceOffering.getId());
        response.setServiceOfferingId(providerServiceOffering.getServiceOffering().getId());
        response.setTitle(providerServiceOffering.getServiceOffering().getTitle());
        response.setDescription(providerServiceOffering.getServiceOffering().getDescription());
        response.setCategory(providerServiceOffering.getServiceOffering().getCategory());
        response.setEffectiveDuration(providerServiceOffering.getDurationOverride() != null ?
                providerServiceOffering.getDurationOverride() :
                providerServiceOffering.getServiceOffering().getDefaultDuration());
        response.setEffectivePrice(providerServiceOffering.getPriceOverride() != null ?
                providerServiceOffering.getPriceOverride() :
                providerServiceOffering.getServiceOffering().getPriceEstimate());
        response.setIsActive(providerServiceOffering.getIsActive());
        return response;
    }
}