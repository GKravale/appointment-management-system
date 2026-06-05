package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.security.CustomUserDetailsService;
import com.appointment.system.service.NotificationService;
import com.appointment.system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails providerUserDetails;
    private CustomUserDetails clientUserDetails;

    @BeforeEach
    void setUp() {
        Provider providerPerson = new Provider();
        providerPerson.setId(2L);

        User providerU = new User();
        providerU.setId(1L);
        providerU.setUsername("provider@test.com");
        providerU.setEmail("provider@test.com");
        providerU.setPassword("password");
        providerU.setRole(Role.ROLE_PROVIDER);
        providerU.setAccountStatus(AccountStatus.ACTIVE);
        providerU.setPerson(providerPerson);
        providerUserDetails = new CustomUserDetails(providerU);

        Client clientPerson = new Client();
        clientPerson.setId(3L);

        User clientU = new User();
        clientU.setId(2L);
        clientU.setUsername("client@test.com");
        clientU.setEmail("client@test.com");
        clientU.setPassword("password");
        clientU.setRole(Role.ROLE_CLIENT);
        clientU.setAccountStatus(AccountStatus.ACTIVE);
        clientU.setPerson(clientPerson);
        clientUserDetails = new CustomUserDetails(clientU);
    }

//    @Test
//    @DisplayName("GET /auth/login should return login page for unauthenticated users")
//    void loginPage_returnsLoginView() throws Exception {
//        mockMvc.perform(get("/auth/login"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/login"));
//    }
//
//    @Test
//    @DisplayName("GET /auth/register should return register page with model")
//    void registerPage_returnsRegisterView() throws Exception {
//        mockMvc.perform(get("/auth/register"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/register"))
//                .andExpect(model().attributeExists("registerRequest"))
//                .andExpect(model().attributeExists("roles"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/register should redirect to login on successful client registration")
//    void register_validClientRequest_redirectsToLogin() throws Exception {
//        when(userService.usernameExists(anyString())).thenReturn(false);
//        when(userService.emailExists(anyString())).thenReturn(false);
//        doNothing().when(userService).register(any(RegisterRequest.class));
//
//        mockMvc.perform(post("/auth/register")
//                        .with(csrf())
//                        .param("username", "anna123")
//                        .param("email", "anna@test.com")
//                        .param("password", "Password1!")
//                        .param("confirmPassword", "Password1!")
//                        .param("firstName", "Anna")
//                        .param("lastName", "Bērziņa")
//                        .param("phoneNr", "+37122233344")
//                        .param("role", "ROLE_CLIENT"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/login"));
//
//        verify(userService).register(any(RegisterRequest.class));
//    }
//
//    @Test
//    @DisplayName("POST /auth/register should show success message for provider registration pending approval")
//    void register_validProviderRequest_redirectsWithPendingMessage() throws Exception {
//        when(userService.usernameExists(anyString())).thenReturn(false);
//        when(userService.emailExists(anyString())).thenReturn(false);
//        doNothing().when(userService).register(any(RegisterRequest.class));
//
//        mockMvc.perform(post("/auth/register")
//                        .with(csrf())
//                        .param("username", "liga123")
//                        .param("email", "liga@test.com")
//                        .param("password", "Password1!")
//                        .param("confirmPassword", "Password1!")
//                        .param("firstName", "Līga")
//                        .param("lastName", "Ozola")
//                        .param("phoneNr", "+37122233237")
//                        .param("role", "ROLE_PROVIDER"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attribute("successMessage", "Registration submitted. Your account is pending admin" +
//                        " approval."));
//    }
//
//    @Test
//    @DisplayName("POST /auth/register should stay on page when username already taken")
//    void register_usernameTaken_staysOnPage() throws Exception {
//        when(userService.usernameExists("anna123")).thenReturn(true);
//        when(userService.emailExists(anyString())).thenReturn(false);
//
//        mockMvc.perform(post("/auth/register")
//                        .with(csrf())
//                        .param("username", "anna123")
//                        .param("email", "anna@test.com")
//                        .param("password", "Password1!")
//                        .param("confirmPassword", "Password1!")
//                        .param("firstName", "Anna")
//                        .param("lastName", "Bērziņa")
//                        .param("phoneNr", "+37122233344")
//                        .param("role", "ROLE_CLIENT"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/register"));
//
//        verify(userService, never()).register(any());
//    }
//
//    @Test
//    @DisplayName("POST /auth/register should stay on page when email already registered")
//    void register_emailTaken_staysOnPage() throws Exception {
//        when(userService.usernameExists(anyString())).thenReturn(false);
//        when(userService.emailExists("anna@test.com")).thenReturn(true);
//
//        mockMvc.perform(post("/auth/register")
//                        .with(csrf())
//                        .param("username", "anna123")
//                        .param("email", "anna@test.com")
//                        .param("password", "Password1!")
//                        .param("confirmPassword", "Password1!")
//                        .param("firstName", "Anna")
//                        .param("lastName", "Bērziņa")
//                        .param("phoneNr", "+37122233344")
//                        .param("role", "ROLE_CLIENT"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/register"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/register should stay on page when passwords do not match")
//    void register_passwordMismatch_staysOnPage() throws Exception {
//        when(userService.usernameExists(anyString())).thenReturn(false);
//        when(userService.emailExists(anyString())).thenReturn(false);
//
//        mockMvc.perform(post("/auth/register")
//                        .with(csrf())
//                        .param("username", "anna123")
//                        .param("email", "anna@test.com")
//                        .param("password", "Password1!")
//                        .param("confirmPassword", "DifferentPassword!")
//                        .param("firstName", "Anna")
//                        .param("lastName", "Bērziņa")
//                        .param("phoneNr", "+37122233344")
//                        .param("role", "ROLE_CLIENT"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/register"));
//
//        verify(userService, never()).register(any());
//    }
//
//    @Test
//    @DisplayName("GET /auth/change-password should return form for authenticated user")
//    @WithMockUser
//    void changePasswordPage_authenticated_returnsView() throws Exception {
//        mockMvc.perform(get("/auth/change-password"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/change-password"))
//                .andExpect(model().attributeExists("changeRequest"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/change-password should redirect provider to profile on success")
//    void changePassword_validRequest_redirectsProviderToProfile() throws Exception {
//        doNothing().when(userService).changePassword(anyLong(), any());
//
//        mockMvc.perform(post("/auth/change-password")
//                        .with(user(providerUserDetails))
//                        .with(csrf())
//                        .param("currentPassword", "OldPass1!")
//                        .param("newPassword", "NewPass1!")
//                        .param("confirmNewPassword", "NewPass1!"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/profile"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/change-password should redirect client to profile on success")
//    void changePassword_validRequest_redirectsClientToProfile() throws Exception {
//        doNothing().when(userService).changePassword(anyLong(), any());
//
//        mockMvc.perform(post("/auth/change-password")
//                        .with(user(clientUserDetails))
//                        .with(csrf())
//                        .param("currentPassword", "OldPass1!")
//                        .param("newPassword", "NewPass1!")
//                        .param("confirmNewPassword", "NewPass1!"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/client/profile"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/change-password should redirect with error when current password is wrong")
//    void changePassword_wrongCurrentPassword_redirectsWithError() throws Exception {
//        doThrow(new IllegalArgumentException("Current password is incorrect"))
//                .when(userService).changePassword(anyLong(), any());
//
//        mockMvc.perform(post("/auth/change-password")
//                        .with(user(providerUserDetails))
//                        .with(csrf())
//                        .param("currentPassword", "WrongPass!")
//                        .param("newPassword", "NewPass1!")
//                        .param("confirmNewPassword", "NewPass1!"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("GET /auth/forgot-password should return forgot password page")
//    void forgotPasswordPage_returnsView() throws Exception {
//        mockMvc.perform(get("/auth/forgot-password"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/forgot-password"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/forgot-password should redirect to login with message")
//    void forgotPassword_validEmail_redirectsToLogin() throws Exception {
//        doNothing().when(userService).initiatePasswordReset(any());
//
//        mockMvc.perform(post("/auth/forgot-password").with(csrf()).param("email", "anna@test.com"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/login"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("GET /auth/reset-password should return reset page with token in model")
//    void resetPasswordPage_returnsViewWithToken() throws Exception {
//        mockMvc.perform(get("/auth/reset-password").param("token", "abc123"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/reset-password"))
//                .andExpect(model().attribute("token", "abc123"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/reset-password should redirect to login on success")
//    void resetPassword_validToken_redirectsToLogin() throws Exception {
//        doNothing().when(userService).resetPassword(any());
//
//        mockMvc.perform(post("/auth/reset-password")
//                        .with(csrf())
//                        .param("token", "abc123")
//                        .param("newPassword", "NewPass1!")
//                        .param("confirmNewPassword", "NewPass1!"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/login"));
//    }
//
//    @Test
//    @DisplayName("POST /auth/reset-password should stay on page with error for invalid/expired token")
//    void resetPassword_invalidToken_staysOnPage() throws Exception {
//        doThrow(new IllegalArgumentException("Invalid or expired token")).when(userService).resetPassword(any());
//
//        mockMvc.perform(post("/auth/reset-password")
//                        .with(csrf())
//                        .param("token", "bad-token")
//                        .param("newPassword", "NewPass1!")
//                        .param("confirmNewPassword", "NewPass1!"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/reset-password"))
//                .andExpect(model().attributeExists("errorMessage"));
//    }
}