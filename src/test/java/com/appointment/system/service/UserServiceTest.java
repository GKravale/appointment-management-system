package com.appointment.system.service;

import com.appointment.system.dto.request.*;
import com.appointment.system.entity.*;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest clientRegisterRequest;
    private RegisterRequest providerRegisterRequest;

    @BeforeEach
    void setUp() {
        clientRegisterRequest = new RegisterRequest();
        clientRegisterRequest.setUsername("anna123");
        clientRegisterRequest.setEmail("anna@test.com");
        clientRegisterRequest.setPassword("Password1!");
        clientRegisterRequest.setConfirmPassword("Password1!");
        clientRegisterRequest.setFirstName("Anna");
        clientRegisterRequest.setLastName("Bērziņa");
        clientRegisterRequest.setPhoneNr("+37122233344");
        clientRegisterRequest.setRole(Role.ROLE_CLIENT);

        providerRegisterRequest = new RegisterRequest();
        providerRegisterRequest.setUsername("liga123");
        providerRegisterRequest.setEmail("liga@test.com");
        providerRegisterRequest.setPassword("Password1!");
        providerRegisterRequest.setConfirmPassword("Password1!");
        providerRegisterRequest.setFirstName("Līga");
        providerRegisterRequest.setLastName("Ozola");
        providerRegisterRequest.setPhoneNr("+37122233237");
        providerRegisterRequest.setRole(Role.ROLE_PROVIDER);
    }


//    @Test
//    @DisplayName("Should register client successfully")
//    void register_validClientRequest_savesClientAndUser() {
//        when(userRepository.existsByUsername("anna123")).thenReturn(false);
//        when(userRepository.existsByEmail("anna@test.com")).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
//
//        userService.register(clientRegisterRequest);
//
//        verify(clientRepository).save(any(Client.class));
//        verify(userRepository).save(any(User.class));
//    }

//    @Test
//    @DisplayName("Should register provider with PENDING status")
//    void register_validProviderRequest_savesProviderWithPendingStatus() {
//        when(userRepository.existsByUsername("liga123")).thenReturn(false);
//        when(userRepository.existsByEmail("liga@test.com")).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
//
//        userService.register(providerRegisterRequest);
//
//        verify(providerRepository).save(any(Provider.class));
//        verify(userRepository).save(argThat(user -> user.getAccountStatus() == AccountStatus.PENDING));
//    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when username is already taken")
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("anna123")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(clientRegisterRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email is already registered")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("anna@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(clientRegisterRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when passwords do not match")
    void register_passwordMismatch_throws() {
        clientRegisterRequest.setConfirmPassword("Different1!");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.register(clientRegisterRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when trying to register as admin")
    void register_adminRole_throws() {
        clientRegisterRequest.setRole(Role.ROLE_ADMIN);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.register(clientRegisterRequest));
    }

    @Test
    @DisplayName("Should return true when username exists")
    void usernameExists_existingUsername_returnsTrue() {
        when(userRepository.existsByUsername("anna123")).thenReturn(true);
        assertTrue(userService.usernameExists("anna123"));
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void usernameExists_newUsername_returnsFalse() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        assertFalse(userService.usernameExists("newuser"));
    }

    @Test
    @DisplayName("Should return true when email exists")
    void emailExists_existingEmail_returnsTrue() {
        when(userRepository.existsByEmail("anna@test.com")).thenReturn(true);
        assertTrue(userService.emailExists("anna@test.com"));
    }

    @Test
    @DisplayName("Should change password when current password is correct")
    void changePassword_correctCurrent_updatesPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("hashedOld");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass1!");
        request.setNewPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "hashedOld")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("hashedNew");

        userService.changePassword(1L, request);

        assertEquals("hashedNew", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when current password is wrong")
    void changePassword_wrongCurrentPassword_throws() {
        User user = new User();
        user.setPassword("Old!");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("WrongPass!");
        request.setNewPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass!", "Old!")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when new passwords do not match")
    void changePassword_newPasswordMismatch_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass1!");
        request.setNewPassword("NewPass1!");
        request.setConfirmPassword("Different1!");

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found on changePassword")
    void changePassword_userNotFound_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass1!");
        request.setNewPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.changePassword(99L, request));
    }

    @Test
    @DisplayName("Should send reset email when user with email exists")
    void initiatePasswordReset_existingEmail_sendsEmail() {
        User user = new User();
        user.setEmail("anna@test.com");
        user.setUsername("anna123");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("anna@test.com");

        when(userRepository.findByEmail("anna@test.com")).thenReturn(Optional.of(user));

        userService.initiatePasswordReset(request);

        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("anna@test.com"), eq("anna123"), anyString());
    }

    @Test
    @DisplayName("Should do nothing silently when email not found for password reset")
    void initiatePasswordReset_emailNotFound_doesNothing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("unknown@test.com");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.initiatePasswordReset(request));
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should reset password with valid token")
    void resetPassword_validToken_updatesPassword() {
        User user = new User();
        user.setPassword("oldHash");

        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(false);
        when(token.getUsed()).thenReturn(false);
        when(token.getUser()).thenReturn(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("newHash");

        userService.resetPassword(request);

        assertEquals("newHash", user.getPassword());
        verify(token).setUsed(true);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when token is expired")
    void resetPassword_expiredToken_throws() {
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(true);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when token is already used")
    void resetPassword_usedToken_throws() {
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(false);
        when(token.getUsed()).thenReturn(true);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("used-token");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when token not found")
    void resetPassword_tokenNotFound_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad-token");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when reset passwords do not match")
    void resetPassword_passwordMismatch_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("some-token");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("Different1!");

        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(request));
        verify(passwordResetTokenRepository, never()).findByToken(any());
    }

    @Test
    @DisplayName("Should soft-delete user account and mark person as deleted")
    void deleteAccount_existingUser_softDeletes() {
        Client person = new Client();
        person.setIsDeleted(false);

        User user = new User();
        user.setId(1L);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPerson(person);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteAccount(1L);

        assertEquals(AccountStatus.SUSPENDED, user.getAccountStatus());
        assertTrue(person.getIsDeleted());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found on deleteAccount")
    void deleteAccount_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.deleteAccount(99L));
    }
}