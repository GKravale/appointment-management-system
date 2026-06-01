package com.appointment.system.service;

import com.appointment.system.dto.request.UpdateClientProfileRequest;
import com.appointment.system.dto.response.ClientProfileResponse;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.User;
import com.appointment.system.repository.ClientRepository;
import com.appointment.system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ClientService clientService;

    private Client client() {
        Client client = new Client();
        client.setId(1L);
        client.setFirstName("Anna");
        client.setLastName("Bērziņa");
        client.setPhoneNr("+37122233344");
        return client;
    }

    @Test
    @DisplayName("Should return profile for existing client")
    void getProfile_returnsProfile() {
        Client client = client();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findByPersonId(1L)).thenReturn(Optional.empty());

        ClientProfileResponse result = clientService.getProfile(1L);

        assertNotNull(result);
        assertEquals("Anna", result.getFirstName());
        assertEquals("Bērziņa", result.getLastName());
    }

    @Test
    @DisplayName("Should include email in profile when user record exists")
    void getProfile_includesEmail_whenUserExists() {
        Client client = client();
        User user = new User();
        user.setEmail("anna@test.com");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findByPersonId(1L)).thenReturn(Optional.of(user));

        ClientProfileResponse result = clientService.getProfile(1L);

        assertEquals("anna@test.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when client not found on getProfile")
    void getProfile_notFound_throws() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> clientService.getProfile(99L));
    }

    @Test
    @DisplayName("Should update client profile fields successfully")
    void updateProfile_updatesFields() {
        Client client = client();

        UpdateClientProfileRequest request = new UpdateClientProfileRequest();
        request.setFirstName("Lāsma");
        request.setLastName("Kalniņa");
        request.setPhoneNr("+37122299999");
        request.setNotes("-");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.updateProfile(1L, request);

        assertEquals("Lāsma", client.getFirstName());
        assertEquals("Kalniņa", client.getLastName());
        assertEquals("+37122299999", client.getPhoneNr());
        assertEquals("-", client.getNotes());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when client not found on updateProfile")
    void updateProfile_notFound_throws() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateClientProfileRequest request = new UpdateClientProfileRequest();
        assertThrows(EntityNotFoundException.class, () -> clientService.updateProfile(99L, request));
    }
}