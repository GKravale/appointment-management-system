package com.appointment.system.service;

import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.ClientRepository;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (request.getRole() == Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Cannot register as admin");
        }

        if (request.getRole() == Role.ROLE_CLIENT) {
            Client client = new Client(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNr()
            );
            clientRepository.save(client);

            User user = new User(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getEmail(),
                    Role.ROLE_CLIENT,
                    client
            );
            userRepository.save(user);

        } else {
            Provider provider = new Provider(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNr(),
                    null,
                    null
            );
            providerRepository.save(provider);

            User user = new User(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getEmail(),
                    Role.ROLE_PROVIDER,
                    provider
            );
            userRepository.save(user);
        }
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}