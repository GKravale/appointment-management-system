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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOfferingServiceTest {

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;
    @Mock
    private ProviderServiceOfferingRepository providerServiceOfferingRepository;
    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ServiceOfferingService serviceOfferingService;

    private Provider provider;
    private ServiceOffering serviceOffering;
    private ProviderServiceOffering providerServiceOffering;

    @BeforeEach
    void setUp() {
        provider = new Provider();
        provider.setId(1L);
        provider.setFirstName("Līga");

        serviceOffering = new ServiceOffering();
        serviceOffering.setId(5L);
        serviceOffering.setTitle("Haircut");
        serviceOffering.setDefaultDuration(60);
        serviceOffering.setPriceEstimate("25.00");
        serviceOffering.setCategory(ServiceCategory.HAIR);
        serviceOffering.setBookingType(BookingType.SLOT_BASED);

        providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setId(10L);
        providerServiceOffering.setProvider(provider);
        providerServiceOffering.setServiceOffering(serviceOffering);
        providerServiceOffering.setIsActive(true);
        providerServiceOffering.setIsDeleted(false);
    }

    @Test
    @DisplayName("Should return active offerings for existing provider")
    void getProviderOfferings_returnsActiveList() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider))
                .thenReturn(List.of(providerServiceOffering));

        List<ProviderServiceOfferingResponse> result = serviceOfferingService.getProviderOfferings(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on getProviderOfferings")
    void getProviderOfferings_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> serviceOfferingService.getProviderOfferings(99L));
    }

    @Test
    @DisplayName("Should return all active base offerings")
    void getAllBaseOfferings_returnsActiveOfferings() {
        when(serviceOfferingRepository.findByIsActiveTrueAndIsDeletedFalse()).thenReturn(List.of(serviceOffering));

        List<ServiceOffering> result = serviceOfferingService.getAllBaseOfferings();

        assertEquals(1, result.size());
        assertEquals("Haircut", result.getFirst().getTitle());
    }

    @Test
    @DisplayName("Should return all service categories")
    void getAllCategories_returnsAllEnumValues() {
        List<ServiceCategory> result = serviceOfferingService.getAllCategories();
        assertEquals(ServiceCategory.values().length, result.size());
    }

    @Test
    @DisplayName("Should create and assign a new service offering with default SLOT_BASED booking type")
    void createAndAssign_noBookingType_defaultsToSlotBased() {
        CreateServiceOfferingRequest request = new CreateServiceOfferingRequest();
        request.setTitle("Manicure");
        request.setDescription("Classic manicure");
        request.setDefaultDuration(45);
        request.setPriceEstimate("20.00");
        request.setCategory(ServiceCategory.NAILS);
        request.setBookingType(null);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        serviceOfferingService.createAndAssign(1L, request);

        verify(serviceOfferingRepository, times(1)).save(any(ServiceOffering.class));
        verify(providerServiceOfferingRepository, times(1)).save(any(ProviderServiceOffering.class));
    }

    @Test
    @DisplayName("Should create service offering with REQUEST_ONLY booking type when specified")
    void createAndAssign_requestOnlyType_savesCorrectly() {
        CreateServiceOfferingRequest request = new CreateServiceOfferingRequest();
        request.setTitle("Tattoo");
        request.setDefaultDuration(120);
        request.setPriceEstimate("80.00");
        request.setCategory(ServiceCategory.TATTOO);
        request.setBookingType(BookingType.REQUEST_ONLY);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        serviceOfferingService.createAndAssign(1L, request);

        verify(serviceOfferingRepository).save(argThat(so -> so.getBookingType() == BookingType.REQUEST_ONLY));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on createAndAssign")
    void createAndAssign_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> serviceOfferingService.createAndAssign(99L,
                new CreateServiceOfferingRequest()));
    }

    @Test
    @DisplayName("Should update service offering fields when provider owns it")
    void update_owner_updatesFields() {
        CreateServiceOfferingRequest request = new CreateServiceOfferingRequest();
        request.setTitle("Updated Haircut");
        request.setDescription("New description");
        request.setDefaultDuration(45);
        request.setPriceEstimate("35.00");
        request.setCategory(ServiceCategory.HAIR);
        request.setBookingType(BookingType.SLOT_BASED);

        when(providerServiceOfferingRepository.findById(10L)).thenReturn(Optional.of(providerServiceOffering));

        serviceOfferingService.update(10L, 1L, request);

        assertEquals("Updated Haircut", serviceOffering.getTitle());
        assertEquals("New description", serviceOffering.getDescription());
        assertEquals(45, serviceOffering.getDefaultDuration());
    }

    @Test
    @DisplayName("Should throw SecurityException when different provider tries to update")
    void update_notOwner_throwsSecurityException() {
        when(providerServiceOfferingRepository.findById(10L)).thenReturn(Optional.of(providerServiceOffering));

        assertThrows(SecurityException.class, () -> serviceOfferingService.update(10L, 99L,
                new CreateServiceOfferingRequest()));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when service not found on update")
    void update_notFound_throws() {
        when(providerServiceOfferingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> serviceOfferingService.update(99L, 1L,
                new CreateServiceOfferingRequest()));
    }

    @Test
    @DisplayName("Should deactivate service offering when provider owns it")
    void deactivate_owner_setsInactive() {
        when(providerServiceOfferingRepository.findById(10L)).thenReturn(Optional.of(providerServiceOffering));

        serviceOfferingService.deactivate(10L, 1L);

        assertFalse(providerServiceOffering.getIsActive());
    }

    @Test
    @DisplayName("Should throw SecurityException when different provider tries to deactivate")
    void deactivate_notOwner_throwsSecurityException() {
        when(providerServiceOfferingRepository.findById(10L)).thenReturn(Optional.of(providerServiceOffering));

        assertThrows(SecurityException.class, () -> serviceOfferingService.deactivate(10L, 99L));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when service not found on deactivate")
    void deactivate_notFound_throws() {
        when(providerServiceOfferingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> serviceOfferingService.deactivate(99L, 1L));
    }
}