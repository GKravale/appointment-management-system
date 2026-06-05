package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.dto.response.ClientProfileResponse;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.*;
import jakarta.persistence.EntityNotFoundException;
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

@WebMvcTest(ClientController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;
    @MockitoBean
    private ProviderService providerService;
    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private ServiceOfferingService serviceOfferingService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ProviderServiceOfferingRepository providerServiceOfferingRepository;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PostService postService;

    private CustomUserDetails clientUser;

    @BeforeEach
    void setUp() {
        Client person = new Client();
        person.setId(3L);

        User user = new User();
        user.setId(3L);
        user.setUsername("client@test.com");
        user.setEmail("client@test.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_CLIENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPerson(person);

        clientUser = new CustomUserDetails(user);
    }

//    @Test
//    @DisplayName("GET /client/dashboard should return 200 for authenticated client")
//    void dashboard_authenticated_returns200() throws Exception {
//        when(clientService.getProfile(anyLong())).thenReturn(new ClientProfileResponse());
//        when(appointmentService.getClientAppointments(anyLong())).thenReturn(List.of());
//
//        mockMvc.perform(get("/client/dashboard").with(user(clientUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("client/dashboard"));
//    }
//
//    @Test
//    @DisplayName("GET /client/appointments should return appointments list")
//    void appointments_authenticated_returnsAppointmentsView() throws Exception {
//        AppointmentResponse appointmentResponse = new AppointmentResponse();
//        appointmentResponse.setId(1L);
//        appointmentResponse.setStatus(AppointmentStatus.CONFIRMED);
//        appointmentResponse.setStartTime(LocalDateTime.now().plusDays(1));
//        appointmentResponse.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
//        appointmentResponse.setServiceTitleSnapshot("Haircut");
//
//        when(appointmentService.getClientAppointments(anyLong())).thenReturn(List.of(appointmentResponse));
//
//        mockMvc.perform(get("/client/appointments").with(user(clientUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("client/appointments"))
//                .andExpect(model().attributeExists("appointments"));
//    }
//
//    @Test
//    @DisplayName("GET /client/book should return booking page with provider and service")
//    void bookingPage_validProviderAndService_returnsBookView() throws Exception {
//        ProviderProfileResponse profile = new ProviderProfileResponse();
//        profile.setId(2L);
//        when(providerService.getPublicProfile(anyLong())).thenReturn(profile);
//        when(serviceOfferingService.getProviderOfferings(anyLong())).thenReturn(List.of());
//        when(appointmentService.getAvailableSlots(anyLong(), any())).thenReturn(List.of());
//
//        mockMvc.perform(get("/client/book").with(user(clientUser))
//                        .param("providerId", "2")
//                        .param("serviceId", "10"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("client/book"));
//    }
//
//    @Test
//    @DisplayName("GET /client/book should redirect when provider not found")
//    void bookingPage_providerNotFound_redirects() throws Exception {
//        when(providerService.getPublicProfile(anyLong())).thenThrow(new EntityNotFoundException("Provider not found"));
//
//        mockMvc.perform(get("/client/book").with(user(clientUser))
//                        .param("providerId", "99")
//                        .param("serviceId", "10"))
//                .andExpect(status().is3xxRedirection());
//    }
//
//    @Test
//    @DisplayName("POST /client/book should redirect to appointments on success")
//    void confirmBooking_validRequest_redirectsToAppointments() throws Exception {
//        doNothing().when(appointmentService).book(anyLong(), any());
//
//        mockMvc.perform(post("/client/book").with(user(clientUser)).with(csrf())
//                        .param("providerServiceOfferingId", "10")
//                        .param("startTime", LocalDateTime.now().plusDays(2).toString()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/client/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /client/book should redirect with error when service not found")
//    void confirmBooking_serviceNotFound_redirectsWithError() throws Exception {
//        doThrow(new EntityNotFoundException("Service not found")).when(appointmentService).book(anyLong(), any());
//
//        mockMvc.perform(post("/client/book").with(user(clientUser)).with(csrf())
//                        .param("providerServiceOfferingId", "99")
//                        .param("startTime", LocalDateTime.now().plusDays(2).toString()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /client/appointments/{id}/cancel should redirect on success")
//    void cancelAppointment_validAppointment_redirects() throws Exception {
//        doNothing().when(appointmentService).cancelByClient(anyLong(), anyLong());
//
//        mockMvc.perform(post("/client/appointments/1/cancel").with(user(clientUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/client/appointments"));
//    }
//
//    @Test
//    @DisplayName("POST /client/appointments/{id}/cancel should show error when past cut-off")
//    void cancelAppointment_pastCutoff_redirectsWithError() throws Exception {
//        doThrow(new IllegalStateException("Cancellation is no longer possible"))
//                .when(appointmentService).cancelByClient(anyLong(), anyLong());
//
//        mockMvc.perform(post("/client/appointments/1/cancel").with(user(clientUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /client/appointments/{id}/cancel should show error for wrong client")
//    void cancelAppointment_wrongClient_redirectsWithError() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(appointmentService).cancelByClient(anyLong(), anyLong());
//
//        mockMvc.perform(post("/client/appointments/1/cancel").with(user(clientUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("GET /client/profile should return profile page with model")
//    void profilePage_authenticated_returnsProfileView() throws Exception {
//        ClientProfileResponse profile = new ClientProfileResponse();
//        profile.setFirstName("Anna");
//        when(clientService.getProfile(anyLong())).thenReturn(profile);
//
//        mockMvc.perform(get("/client/profile").with(user(clientUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("client/profile"))
//                .andExpect(model().attributeExists("profile"))
//                .andExpect(model().attributeExists("updateRequest"));
//    }
//
//    @Test
//    @DisplayName("POST /client/profile should redirect on valid update")
//    void updateProfile_validRequest_redirects() throws Exception {
//        doNothing().when(clientService).updateProfile(anyLong(), any());
//
//        mockMvc.perform(post("/client/profile").with(user(clientUser)).with(csrf())
//                        .param("firstName", "Anna")
//                        .param("lastName", "Bērziņa")
//                        .param("phoneNr", "+37122233344"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/client/profile"));
//    }
//
//    @Test
//    @DisplayName("POST /client/profile should stay on page when validation fails")
//    void updateProfile_invalidRequest_staysOnPage() throws Exception {
//        mockMvc.perform(post("/client/profile").with(user(clientUser)).with(csrf())
//                        .param("firstName", "")
//                        .param("lastName", "Bērziņa")
//                        .param("phoneNr", "+37122233344"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("client/profile"));
//    }
//
//    @Test
//    @DisplayName("GET /client/slots should return JSON list of available slots")
//    void getSlots_validParams_returnsJsonList() throws Exception {
//        when(appointmentService.getAvailableSlots(anyLong(), any())).thenReturn(List.of());
//
//        mockMvc.perform(get("/client/slots").with(user(clientUser))
//                        .param("serviceId", "10")
//                        .param("date", "2025-06-10"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith("application/json"));
//    }
//
//    @Test
//    @DisplayName("GET /client/slots should return empty list when service not found")
//    void getSlots_serviceNotFound_returnsEmptyList() throws Exception {
//        when(appointmentService.getAvailableSlots(anyLong(), any())).thenThrow(new EntityNotFoundException("Service " +
//                "not found"));
//
//        mockMvc.perform(get("/client/slots").with(user(clientUser))
//                        .param("serviceId", "99")
//                        .param("date", "2025-06-10"))
//                .andExpect(status().isOk())
//                .andExpect(content().json("[]"));
//    }
}