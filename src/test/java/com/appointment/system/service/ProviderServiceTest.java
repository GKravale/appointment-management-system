package com.appointment.system.service;

import com.appointment.system.dto.request.UpdateProviderProfileRequest;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.entity.*;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private ProviderServiceOfferingRepository providerServiceOfferingRepository;

    @InjectMocks
    private ProviderService providerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(providerService, "uploadDir", System.getProperty("java.io.tmpdir"));
    }

    private Provider provider(Long id, String firstName, String lastName) {
        Provider provider = new Provider();
        provider.setId(id);
        provider.setFirstName(firstName);
        provider.setLastName(lastName);
        provider.setIsActive(true);
        provider.setIsDeleted(false);
        return provider;
    }

    private ProviderServiceOffering offering(Provider provider, String title, ServiceCategory category) {
        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setTitle(title);
        serviceOffering.setCategory(category);
        serviceOffering.setDefaultDuration(60);
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setProvider(provider);
        providerServiceOffering.setServiceOffering(serviceOffering);
        return providerServiceOffering;
    }

    @Test
    @DisplayName("Should return profile for existing provider")
    void getProfile_returnsProfile() {
        Provider provider = provider(1L, "Līga", "Ozola");
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider))
                .thenReturn(List.of());

        ProviderProfileResponse result = providerService.getProfile(1L);

        assertNotNull(result);
        assertEquals("Līga", result.getFirstName());
        assertEquals("Ozola", result.getLastName());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on getProfile")
    void getProfile_notFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> providerService.getProfile(99L));
    }

    @Test
    @DisplayName("Should update provider profile fields")
    void updateProfile_updatesAllFields() {
        Provider provider = provider(1L, "Līga", "Ozola");
        UpdateProviderProfileRequest request = new UpdateProviderProfileRequest();
        request.setFirstName("Marta");
        request.setLastName("Kalniņa");
        request.setPhoneNr("+37122299999");
        request.setLocation("Rīga");
        request.setBio("-");
        request.setCancellationHoursLimit(48);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        providerService.updateProfile(1L, request);

        assertEquals("Marta", provider.getFirstName());
        assertEquals("Kalniņa", provider.getLastName());
        assertEquals("+37122299999", provider.getPhoneNr());
        assertEquals("Rīga", provider.getLocation());
        assertEquals("-", provider.getBio());
        assertEquals(48, provider.getCancellationHoursLimit());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on updateProfile")
    void updateProfile_notFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> providerService.updateProfile(99L,
                new UpdateProviderProfileRequest()));
    }

    @Test
    @DisplayName("Should save profile picture path and call repository save")
    void uploadProfilePicture_validImage_savesPath() throws IOException {
        Provider provider = provider(1L, "Līga", "Ozola");
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[100]);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        providerService.uploadProfilePicture(1L, file);

        assertNotNull(provider.getProfilePicturePath());
        assertTrue(provider.getProfilePicturePath().startsWith("uploads/profiles/"));
        assertTrue(provider.getProfilePicturePath().endsWith(".jpg"));
        verify(providerRepository).save(provider);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when uploaded file is empty")
    void uploadProfilePicture_emptyFile_throws() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> providerService.uploadProfilePicture(1L, emptyFile));
        verify(providerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when uploaded file is not an image")
    void uploadProfilePicture_notImage_throws() {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[100]);

        assertThrows(IllegalArgumentException.class, () -> providerService.uploadProfilePicture(1L, pdfFile));
        verify(providerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return only active non-deleted providers")
    void getAllActiveProviders_returnsOnlyActive() {
        Provider provider1 = provider(1L, "Līga", "Ozola");
        Provider provider2 = provider(2L, "Marta", "Kalniņa");

        when(providerRepository.findByIsActiveTrueAndIsDeletedFalse()).thenReturn(List.of(provider1, provider2));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(any())).thenReturn(List.of());

        List<ProviderProfileResponse> result = providerService.getAllActiveProviders();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should filter providers by category")
    void getActiveProvidersByCategory_returnsMatchingProviders() {
        Provider provider1 = provider(1L, "Līga", "Ozola");
        Provider provider2 = provider(2L, "Marta", "Kalniņa");
        ProviderServiceOffering hairOffering = offering(provider1, "Haircut", ServiceCategory.HAIR);
        ProviderServiceOffering nailOffering = offering(provider2, "Manicure", ServiceCategory.NAILS);

        when(providerRepository.findByIsActiveTrueAndIsDeletedFalse()).thenReturn(List.of(provider1, provider2));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider1))
                .thenReturn(List.of(hairOffering));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider2))
                .thenReturn(List.of(nailOffering));

        List<ProviderProfileResponse> result = providerService.getActiveProvidersByCategory(ServiceCategory.HAIR);

        assertEquals(1, result.size());
        assertEquals("Līga", result.getFirst().getFirstName());
    }

    @Test
    @DisplayName("Should return empty list when no providers match the category")
    void getActiveProvidersByCategory_noMatch_returnsEmpty() {
        Provider provider = provider(1L, "Līga", "Ozola");
        ProviderServiceOffering hairOffering = offering(provider, "Haircut", ServiceCategory.HAIR);

        when(providerRepository.findByIsActiveTrueAndIsDeletedFalse()).thenReturn(List.of(provider));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider))
                .thenReturn(List.of(hairOffering));

        List<ProviderProfileResponse> result = providerService.getActiveProvidersByCategory(ServiceCategory.TATTOO);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return public profile for any provider by id")
    void getPublicProfile_returnsProfile() {
        Provider provider = provider(1L, "Līga", "Ozola");
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider))
                .thenReturn(List.of());

        ProviderProfileResponse result = providerService.getPublicProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException for non-existent provider on getPublicProfile")
    void getPublicProfile_notFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> providerService.getPublicProfile(99L));
    }
}