package com.appointment.system.service;

import com.appointment.system.dto.request.UpdateClientProfileRequest;
import com.appointment.system.dto.response.ClientProfileResponse;
import com.appointment.system.entity.Client;
import com.appointment.system.repository.ClientRepository;
import com.appointment.system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;

    public ClientProfileResponse getProfile(Long personId) {
        log.debug("Retrieving profile for client {}", personId);
        Client client = clientRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Client not" +
                " found"));
        return toResponse(client);
    }

    @Transactional
    public void updateProfile(Long personId, UpdateClientProfileRequest request) {
        Client client = clientRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Client not" +
                " found"));
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhoneNr(request.getPhoneNr());
        client.setNotes(request.getNotes());
        log.info("Client profile updated: id={}", personId);
    }

    private ClientProfileResponse toResponse(Client client) {
        ClientProfileResponse response = new ClientProfileResponse();
        response.setId(client.getId());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setPhoneNr(client.getPhoneNr());
        response.setNotes(client.getNotes());
        userRepository.findByPersonId(client.getId()).ifPresent(u -> response.setEmail(u.getEmail()));
        return response;
    }
}