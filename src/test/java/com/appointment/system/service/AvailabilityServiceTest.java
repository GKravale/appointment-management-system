package com.appointment.system.service;

import com.appointment.system.dto.request.CreateAvailabilityRequest;
import com.appointment.system.dto.response.AvailabilityResponse;
import com.appointment.system.entity.Availability;
import com.appointment.system.entity.Provider;
import com.appointment.system.repository.AvailabilityRepository;
import com.appointment.system.repository.ProviderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private ProviderRepository providerRepository;
    @InjectMocks
    private AvailabilityService availabilityService;

    private Provider provider() {
        Provider provider = new Provider();
        provider.setId(1L);
        return provider;
    }

    private Availability availability(Provider p, LocalTime start, LocalTime end) {
        Availability availability = new Availability();
        availability.setId(1L);
        availability.setProvider(p);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(start);
        availability.setEndTime(end);
        return availability;
    }

    @Test
    @DisplayName("Should return availability list for existing provider")
    void getAvailability_returnsListForProvider() {
        Provider provider = provider();
        Availability availability = availability(provider, LocalTime.of(9, 0), LocalTime.of(17, 0));

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(availabilityRepository.findByProvider(provider)).thenReturn(List.of(availability));

        List<AvailabilityResponse> result = availabilityService.getAvailability(1L);

        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.getFirst().getDayOfWeek());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on getAvailability")
    void getAvailability_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> availabilityService.getAvailability(99L));
    }

    @Test
    @DisplayName("Should create availability when no overlap exists")
    void create_noOverlap_savesAvailability() {
        Provider provider = provider();
        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(availabilityRepository.findByProviderAndDayOfWeek(provider, DayOfWeek.MONDAY)).thenReturn(List.of());

        availabilityService.create(1L, request);

        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when start time is after end time")
    void create_startAfterEnd_throws() {
        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(17, 0));
        request.setEndTime(LocalTime.of(9, 0));

        assertThrows(IllegalArgumentException.class, () -> availabilityService.create(1L, request));
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when start equals end time")
    void create_startEqualsEnd_throws() {
        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 0));

        assertThrows(IllegalArgumentException.class, () -> availabilityService.create(1L, request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when new slot overlaps existing availability")
    void create_overlaps_throws() {
        Provider provider = provider();
        Availability existing = availability(provider, LocalTime.of(9, 0), LocalTime.of(13, 0));

        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(14, 0));

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(availabilityRepository.findByProviderAndDayOfWeek(provider, DayOfWeek.MONDAY))
                .thenReturn(List.of(existing));

        assertThrows(IllegalArgumentException.class, () -> availabilityService.create(1L, request));
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete availability when provider owns it")
    void delete_ownerDeletes_success() {
        Provider provider = provider();
        Availability availability = availability(provider, LocalTime.of(9, 0), LocalTime.of(17, 0));

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        availabilityService.delete(1L, 1L);

        verify(availabilityRepository, times(1)).delete(availability);
    }

    @Test
    @DisplayName("Should throw SecurityException when provider does not own the availability")
    void delete_notOwner_throwsSecurityException() {
        Provider provider = provider();
        Availability availability = availability(provider, LocalTime.of(9, 0), LocalTime.of(17, 0));

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        assertThrows(SecurityException.class, () -> availabilityService.delete(1L, 99L));
        verify(availabilityRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when availability not found on delete")
    void delete_notFound_throws() {
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> availabilityService.delete(99L, 1L));
    }

    @Test
    @DisplayName("Should return all days of the week")
    void getAllDays_returnsAllSevenDays() {
        List<DayOfWeek> days = availabilityService.getAllDays();
        assertEquals(7, days.size());
    }
}