package com.appointment.system.service;

import com.appointment.system.dto.request.ChangePasswordRequest;
import com.appointment.system.dto.request.ForgotPasswordRequest;
import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.dto.request.ResetPasswordRequest;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.EmailVerificationToken;
import com.appointment.system.entity.PasswordResetToken;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.ClientRepository;
import com.appointment.system.repository.EmailVerificationTokenRepository;
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
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
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

        User user;
        if (request.getRole() == Role.ROLE_CLIENT) {
            Client client = new Client(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNr()
            );
            clientRepository.save(client);

            user = new User(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getEmail(),
                    Role.ROLE_CLIENT,
                    client
            );
            user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        } else {
            Provider provider = new Provider(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNr(),
                    null,
                    null
            );
            providerRepository.save(provider);

            user = new User(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getEmail(),
                    Role.ROLE_PROVIDER,
                    provider
            );
            user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        }
        userRepository.save(user);

        String token = java.util.UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(new EmailVerificationToken(user, token));
        emailService.sendEmailVerification(user.getEmail(), user.getUsername(), token);

        log.info("Registered user: {} — awaiting email verification", user.getUsername());
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid verification link"));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("This verification link has expired. Please request a new one.");
        }
        if (verificationToken.getUsed()) {
            throw new IllegalArgumentException("This verification link has already been used.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);

        // Clients become ACTIVE immediately; providers still need admin approval
        if (user.getRole() == Role.ROLE_CLIENT) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        } else {
            user.setAccountStatus(AccountStatus.PENDING);
        }

        userRepository.save(user);
        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
        log.info("Email verified for user: {}", user.getUsername());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No account found with that email"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("Email is already verified");
        }

        emailVerificationTokenRepository.deleteByUser(user);
        String token = java.util.UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(new EmailVerificationToken(user, token));
        emailService.sendEmailVerification(user.getEmail(), user.getUsername(), token);
        log.info("Resent verification email for user: {}", user.getUsername());
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

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
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

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.getPerson().setIsDeleted(true);
        userRepository.save(user);
        log.info("Account deleted for user: {}", user.getUsername());
    }
}