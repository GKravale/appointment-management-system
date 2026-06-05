package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProviderController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderService providerService;
    @MockitoBean
    private AppointmentService appointmentService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private PostService postService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails providerUser;

    @BeforeEach
    void setUp() {
        Provider person = new Provider();
        person.setId(2L);

        User user = new User();
        user.setId(1L);
        user.setUsername("provider@test.com");
        user.setEmail("provider@test.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_PROVIDER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPerson(person);

        providerUser = new CustomUserDetails(user);
    }

//    @Test
//    @DisplayName("GET /provider/dashboard should return 200 for authenticated provider")
//    @WithMockUser(roles = "PROVIDER")
//    void dashboard_authenticated_returns200() throws Exception {
//        when(providerService.getProfile(anyLong())).thenReturn(new ProviderProfileResponse());
//        when(appointmentService.getProviderAppointments(anyLong())).thenReturn(List.of());
//
//        mockMvc.perform(get("/provider/dashboard").with(user(providerUser))).andExpect(status().isOk())
//                .andExpect(view().name("provider/dashboard"));
//    }
//
//    @Test
//    @DisplayName("GET /provider/profile should render profile page with model attributes")
//    void profilePage_authenticated_returnsProfileView() throws Exception {
//        ProviderProfileResponse profile = new ProviderProfileResponse();
//        profile.setFirstName("Līga");
//        when(providerService.getProfile(anyLong())).thenReturn(profile);
//
//        mockMvc.perform(get("/provider/profile").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/profile"))
//                .andExpect(model().attributeExists("profile"))
//                .andExpect(model().attributeExists("updateRequest"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/profile should redirect back after successful update")
//    void updateProfile_validRequest_redirects() throws Exception {
//        doNothing().when(providerService).updateProfile(anyLong(), any());
//
//        mockMvc.perform(post("/provider/profile").with(user(providerUser)).with(csrf())
//                        .param("firstName", "Līga")
//                        .param("lastName", "Ozola")
//                        .param("phoneNr", "+37122233237"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/profile"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/profile should stay on page when validation fails")
//    void updateProfile_invalidRequest_staysOnPage() throws Exception {
//        mockMvc.perform(post("/provider/profile").with(user(providerUser)).with(csrf())
//                        .param("firstName", "")
//                        .param("lastName", "Ozola")
//                        .param("phoneNr", "+37122233237"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/profile"));
//    }
//
//    @Test
//    @DisplayName("GET /provider/appointments should render appointments list")
//    void appointments_authenticated_returnsAppointmentsView() throws Exception {
//        AppointmentResponse appointmentResponse = new AppointmentResponse();
//        appointmentResponse.setId(1L);
//        appointmentResponse.setStatus(AppointmentStatus.REQUESTED);
//        appointmentResponse.setStartTime(LocalDateTime.now().plusDays(1));
//        appointmentResponse.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
//        appointmentResponse.setClientFirstName("Anna");
//        appointmentResponse.setClientLastName("Bērziņa");
//        appointmentResponse.setServiceTitleSnapshot("Haircut");
//
//        when(appointmentService.getProviderAppointments(anyLong())).thenReturn(List.of(appointmentResponse));
//
//        mockMvc.perform(get("/provider/appointments").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/appointments"))
//                .andExpect(model().attributeExists("appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/confirm should redirect on success")
//    void confirm_validAppointment_redirects() throws Exception {
//        doNothing().when(appointmentService).confirm(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/confirm").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/confirm should add error when not authorized")
//    void confirm_unauthorized_redirectsWithError() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(appointmentService).confirm(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/confirm").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/decline should redirect on success")
//    void decline_validAppointment_redirects() throws Exception {
//        doNothing().when(appointmentService).decline(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/decline").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/decline should show error for wrong provider")
//    void decline_wrongProvider_redirectsWithError() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(appointmentService).decline(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/decline").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/cancel should redirect on success")
//    void cancelAppointment_validAppointment_redirects() throws Exception {
//        doNothing().when(appointmentService).cancelByProvider(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/cancel").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/cancel should show error on illegal state")
//    void cancelAppointment_completedAppointment_redirectsWithError() throws Exception {
//        doThrow(new IllegalStateException("Cannot cancel a completed appointment"))
//                .when(appointmentService).cancelByProvider(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/cancel").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/complete should redirect on success")
//    void markComplete_validAppointment_redirects() throws Exception {
//        doNothing().when(appointmentService).markCompleted(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/complete").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/complete should show error when not yet ended")
//    void markComplete_appointmentNotEnded_redirectsWithError() throws Exception {
//        doThrow(new IllegalStateException("Cannot mark complete before appointment has ended"))
//                .when(appointmentService).markCompleted(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/complete").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/noshow should redirect on success")
//    void markNoShow_validAppointment_redirects() throws Exception {
//        doNothing().when(appointmentService).markNoShow(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/noshow").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/appointments/{id}/noshow should show error before start time")
//    void markNoShow_beforeStartTime_redirectsWithError() throws Exception {
//        doThrow(new IllegalStateException("Cannot mark no-show before appointment time"))
//                .when(appointmentService).markNoShow(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/appointments/1/noshow").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("GET /provider/schedule should return schedule view")
//    void schedule_authenticated_returnsScheduleView() throws Exception {
//        mockMvc.perform(get("/provider/schedule").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/schedule"));
//    }
//
//    @Test
//    @DisplayName("GET /provider/schedule/events should return JSON list")
//    void scheduleEvents_authenticated_returnsJsonList() throws Exception {
//        when(appointmentService.getProviderAppointments(anyLong())).thenReturn(List.of());
//
//        mockMvc.perform(get("/provider/schedule/events").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith("application/json"));
//    }
}