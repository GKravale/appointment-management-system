package com.appointment.system.service;

import com.appointment.system.dto.request.UpdateProviderProfileRequest;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.dto.response.ProviderServiceOfferingResponse;
import com.appointment.system.entity.Provider;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;

    private final ProviderServiceOfferingRepository providerServiceOfferingRepository;

    public ProviderProfileResponse getProfile(Long personId) {
        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        return toResponse(provider);
    }

    @Transactional
    public void updateProfile(Long personId, UpdateProviderProfileRequest request) {
        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        provider.setFirstName(request.getFirstName());
        provider.setLastName(request.getLastName());
        provider.setPhoneNr(request.getPhoneNr());
        provider.setLocation(request.getLocation());
        provider.setBio(request.getBio());
        provider.setCancellationHoursLimit(request.getCancellationHoursLimit());
    }

    private ProviderProfileResponse toResponse(Provider provider) {
        ProviderProfileResponse response = new ProviderProfileResponse();
        response.setId(provider.getId());
        response.setFirstName(provider.getFirstName());
        response.setLastName(provider.getLastName());
        response.setPhoneNr(provider.getPhoneNr());
        response.setLocation(provider.getLocation());
        response.setBio(provider.getBio());
        response.setIsActive(provider.getIsActive());
        response.setCancellationHoursLimit(provider.getCancellationHoursLimit());
        response.setServiceOfferings(providerServiceOfferingRepository
                .findByProviderAndIsActiveTrueAndIsDeletedFalse(provider)
                .stream()
                .map(ProviderServiceOfferingResponse::from)
                .toList());
        return response;
    }

    public List<ProviderProfileResponse> getAllActiveProviders() {
        return providerRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProviderProfileResponse> getActiveProvidersByCategory(ServiceCategory category) {
        return providerRepository.findByIsActiveTrue()
                .stream()
                .filter(p -> providerServiceOfferingRepository
                        .findByProviderAndIsActiveTrueAndIsDeletedFalse(p)
                        .stream()
                        .anyMatch(providerServiceOffering -> providerServiceOffering
                                .getServiceOffering().getCategory() == category))
                .map(this::toResponse)
                .toList();
    }

    public ProviderProfileResponse getPublicProfile(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        return toResponse(provider);
    }
}