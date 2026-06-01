package com.appointment.system.service;

import com.appointment.system.dto.request.BookAppointmentRequest;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.entity.*;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ProviderServiceOfferingRepository providerServiceOfferingRepository;
    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Provider provider;
    private Client client;
    private User clientUser;
    private User providerUser;
    private ProviderServiceOffering providerServiceOffering;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        provider = new Provider();
        provider.setId(2L);
        provider.setFirstName("Līga");
        provider.setLastName("Ozola");
        provider.setCancellationHoursLimit(24);

        client = new Client();
        client.setId(3L);
        client.setFirstName("Anna");
        client.setLastName("Bērziņa");

        clientUser = new User();
        clientUser.setId(3L);
        clientUser.setPerson(client);
        clientUser.setEmail("anna@test.com");

        providerUser = new User();
        providerUser.setId(2L);
        providerUser.setPerson(provider);
        providerUser.setEmail("liga@test.com");

        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setTitle("Haircut");
        serviceOffering.setDefaultDuration(60);
        serviceOffering.setPriceEstimate("30.00");

        providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setId(10L);
        providerServiceOffering.setProvider(provider);
        providerServiceOffering.setServiceOffering(serviceOffering);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setClient(client);
        appointment.setProvider(provider);
        appointment.setProviderServiceOffering(providerServiceOffering);
        appointment.setStartTime(LocalDateTime.now().plusDays(2));
        appointment.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setServiceTitleSnapshot("Haircut");
    }

    @Test
    @DisplayName("Should book appointment successfully and send notifications")
    void book_validRequest_savesAndNotifies() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setProviderServiceOfferingId(10L);
        request.setStartTime(LocalDateTime.now().plusDays(2));

        when(providerServiceOfferingRepository.findById(10L)).thenReturn(Optional.of(providerServiceOffering));
        when(clientRepository.findById(3L)).thenReturn(Optional.of(client));
        when(userRepository.findById(3L)).thenReturn(Optional.of(clientUser));
        when(userRepository.findByPersonId(2L)).thenReturn(Optional.of(providerUser));

        appointmentService.book(3L, request);

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(notificationService, times(2)).send(any(User.class), anyString(), anyString());
        verify(emailService).sendAppointmentRequested(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when service not found on book")
    void book_serviceNotFound_throws() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setProviderServiceOfferingId(99L);

        when(providerServiceOfferingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> appointmentService.book(3L, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when client not found on book")
    void book_clientNotFound_throws() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setProviderServiceOfferingId(10L);

        when(providerServiceOfferingRepository.findById(10L)).thenReturn(Optional.of(providerServiceOffering));
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> appointmentService.book(99L, request));
    }

    @Test
    @DisplayName("Should confirm appointment and send notification to client")
    void confirm_requestedAppointment_setsConfirmedStatus() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(3L)).thenReturn(Optional.of(clientUser));

        appointmentService.confirm(1L, 2L);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertNotNull(appointment.getConfirmedAt());
        verify(notificationService).send(eq(clientUser), anyString(), anyString());
        verify(emailService).sendAppointmentConfirmed(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw SecurityException when different provider tries to confirm")
    void confirm_wrongProvider_throwsSecurityException() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        assertThrows(SecurityException.class, () -> appointmentService.confirm(1L, 99L));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when appointment not found on confirm")
    void confirm_appointmentNotFound_throws() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> appointmentService.confirm(99L, 2L));
    }

    @Test
    @DisplayName("Should decline appointment and notify client")
    void decline_requestedAppointment_setsDeclinedStatus() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(3L)).thenReturn(Optional.of(clientUser));

        appointmentService.decline(1L, 2L);

        assertEquals(AppointmentStatus.DECLINED, appointment.getStatus());
        verify(notificationService).send(eq(clientUser), anyString(), anyString());
    }

    @Test
    @DisplayName("Should cancel appointment by client when within allowed time window")
    void cancelByClient_withinWindow_setsCancelledStatus() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByPersonId(2L)).thenReturn(Optional.of(providerUser));
        when(userRepository.findById(3L)).thenReturn(Optional.of(clientUser));

        appointmentService.cancelByClient(1L, 3L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertNotNull(appointment.getCancelledAt());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when cancelling within the cut-off window")
    void cancelByClient_pastCutoff_throwsIllegalState() {
        appointment.setStartTime(LocalDateTime.now().plusHours(12));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.cancelByClient(1L, 3L));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to cancel completed appointment")
    void cancelByClient_completedAppointment_throws() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.cancelByClient(1L, 3L));
    }

    @Test
    @DisplayName("Should throw SecurityException when different client tries to cancel")
    void cancelByClient_wrongClient_throwsSecurityException() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        assertThrows(SecurityException.class, () -> appointmentService.cancelByClient(1L, 99L));
    }

    @Test
    @DisplayName("Should cancel appointment by provider and notify client")
    void cancelByProvider_confirmedAppointment_setsCancelledStatus() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(3L)).thenReturn(Optional.of(clientUser));

        appointmentService.cancelByProvider(1L, 2L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(notificationService).send(eq(clientUser), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when cancelling a completed appointment as provider")
    void cancelByProvider_completedAppointment_throws() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.cancelByProvider(1L, 2L));
    }

    @Test
    @DisplayName("Should mark appointment completed when it is confirmed and has ended")
    void markCompleted_confirmedAndPast_setsCompletedStatus() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().minusHours(2));
        appointment.setEndTime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        appointmentService.markCompleted(1L, 2L);

        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        assertNotNull(appointment.getCompletedAt());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when marking not-confirmed appointment as completed")
    void markCompleted_notConfirmed_throws() {
        appointment.setStatus(AppointmentStatus.REQUESTED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.markCompleted(1L, 2L));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when marking completed before appointment ends")
    void markCompleted_appointmentNotYetEnded_throws() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().plusHours(1));
        appointment.setEndTime(LocalDateTime.now().plusHours(2));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.markCompleted(1L, 2L));
    }

    @Test
    @DisplayName("Should mark appointment as no-show when confirmed and start time has passed")
    void markNoShow_confirmedAndPast_setsNoShowStatus() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        appointmentService.markNoShow(1L, 2L);

        assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when marking no-show for a non-confirmed appointment")
    void markNoShow_notConfirmed_throws() {
        appointment.setStatus(AppointmentStatus.REQUESTED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.markNoShow(1L, 2L));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when marking no-show before appointment starts")
    void markNoShow_beforeStartTime_throws() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> appointmentService.markNoShow(1L, 2L));
    }

    @Test
    @DisplayName("Should return appointments for existing client")
    void getClientAppointments_returnsList() {
        when(clientRepository.findById(3L)).thenReturn(Optional.of(client));
        when(appointmentRepository.findByClientOrderByStartTimeDesc(client)).thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.getClientAppointments(3L);

        assertEquals(1, result.size());
        assertEquals("Anna", result.getFirst().getClientFirstName());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when client not found on getClientAppointments")
    void getClientAppointments_clientNotFound_throws() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> appointmentService.getClientAppointments(99L));
    }

    @Test
    @DisplayName("Should return appointments for existing provider")
    void getProviderAppointments_returnsListForProvider() {
        when(providerRepository.findById(2L)).thenReturn(Optional.of(provider));
        when(appointmentRepository.findByProviderOrderByStartTimeDesc(provider)).thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.getProviderAppointments(2L);

        assertEquals(1, result.size());
        assertEquals("Līga", result.getFirst().getProviderFirstName());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on getProviderAppointments")
    void getProviderAppointments_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> appointmentService.getProviderAppointments(99L));
    }
}