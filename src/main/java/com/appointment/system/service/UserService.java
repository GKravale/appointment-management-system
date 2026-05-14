package com.appointment.system.service;

import com.appointment.system.dto.request.ChangePasswordRequest;
import com.appointment.system.dto.request.ForgotPasswordRequest;
import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.dto.request.ResetPasswordRequest;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.PasswordResetToken;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.ClientRepository;
import com.appointment.system.repository.PasswordResetTokenRepository;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

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
            user.setAccountStatus(AccountStatus.PENDING);
            userRepository.save(user);
        }
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(new PasswordResetToken(user, token));

            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);

            log.info("Password reset initiated for user: {}", user.getUsername());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("This reset link has already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset completed for user: {}", user.getUsername());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getUsername());
    }
}