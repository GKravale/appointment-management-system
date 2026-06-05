package com.appointment.system.service;

import com.appointment.system.dto.request.CreateAvailabilityRequest;
import com.appointment.system.dto.response.AvailabilityResponse;
import com.appointment.system.entity.Availability;
import com.appointment.system.entity.Provider;
import com.appointment.system.repository.AvailabilityRepository;
import com.appointment.system.repository.ProviderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ProviderRepository providerRepository;

    public List<AvailabilityResponse> getAvailability(Long personId) {
        log.info("Retrieving availability for provider {}", personId);
        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        return availabilityRepository.findByProvider(provider)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void create(Long personId, CreateAvailabilityRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Provider provider = providerRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        List<Availability> existing = availabilityRepository.findByProviderAndDayOfWeek(provider,
                request.getDayOfWeek());

        boolean overlaps = existing.stream().anyMatch(a ->
                request.getStartTime().isBefore(a.getEndTime()) && request.getEndTime().isAfter(a.getStartTime()));

        if (overlaps) {
            throw new IllegalArgumentException("This time slot overlaps with an existing availability slot for this " +
                    "day");
        }

        Availability availability = new Availability();
        availability.setProvider(provider);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setEffectiveFrom(request.getEffectiveFrom());
        availability.setEffectiveTo(request.getEffectiveTo());
        availabilityRepository.save(availability);
        log.info("Availability created for provider {}: {} {}-{}", personId, request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime());
    }

    @Transactional
    public void delete(Long availabilityId, Long personId) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Availability not found"));
        if (!availability.getProvider().getId().equals(personId)) {
            throw new SecurityException("Not authorized");
        }
        availabilityRepository.delete(availability);
        log.info("Availability deleted: id={}, provider={}", availabilityId, personId);
    }

    public List<DayOfWeek> getAllDays() {
        return List.of(DayOfWeek.values());
    }

    private AvailabilityResponse toResponse(Availability availability) {
        AvailabilityResponse response = new AvailabilityResponse();
        response.setId(availability.getId());
        response.setDayOfWeek(availability.getDayOfWeek());
        response.setStartTime(availability.getStartTime());
        response.setEndTime(availability.getEndTime());
        response.setEffectiveFrom(availability.getEffectiveFrom());
        response.setEffectiveTo(availability.getEffectiveTo());
        return response;
    }
}