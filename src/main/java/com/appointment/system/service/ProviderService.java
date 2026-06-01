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
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderServiceOfferingRepository providerServiceOfferingRepository;

    @Value("${app.upload.dir:uploads}")
    String uploadDir;

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

    @Transactional(rollbackFor = IOException.class)
    public void uploadProfilePicture(Long personId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Please select a file");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(uploadDir, "profiles");
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        provider.setProfilePicturePath("uploads/profiles/" + filename);
        providerRepository.save(provider);
        log.info("Profile picture saved for provider {}: {}", personId, filename);
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
        response.setProfilePicturePath(provider.getProfilePicturePath());
        response.setCancellationHoursLimit(provider.getCancellationHoursLimit());
        response.setServiceOfferings(providerServiceOfferingRepository
                .findByProviderAndIsActiveTrueAndIsDeletedFalse(provider)
                .stream()
                .map(ProviderServiceOfferingResponse::from)
                .toList());
        return response;
    }

    public List<ProviderProfileResponse> getAllActiveProviders() {
        return providerRepository.findByIsActiveTrueAndIsDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProviderProfileResponse> getActiveProvidersByCategory(ServiceCategory category) {
        return providerRepository.findByIsActiveTrueAndIsDeletedFalse()
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