package com.appointment.system.service;

import com.appointment.system.dto.request.UpdateProviderProfileRequest;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.entity.Provider;
import com.appointment.system.repository.ProviderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;

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
        return response;
    }
}