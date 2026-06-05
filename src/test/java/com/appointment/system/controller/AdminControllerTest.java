package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.*;
import com.appointment.system.security.CustomUserDetailsService;
import com.appointment.system.service.AppointmentService;
import com.appointment.system.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ProviderRepository providerRepository;
    @MockitoBean
    private ClientRepository clientRepository;
    @MockitoBean
    private AppointmentRepository appointmentRepository;
    @MockitoBean
    private AppointmentService appointmentService;
    @MockitoBean
    private ServiceOfferingRepository serviceOfferingRepository;
    @MockitoBean
    private ProviderServiceOfferingRepository providerServiceOfferingRepository;
    @MockitoBean
    private NotificationService notificationService;

    private User adminTestUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }

//    @Test
//    @DisplayName("GET /admin/dashboard should return 403 for non-admin users")
//    @WithMockUser(roles = "PROVIDER")
//    void dashboard_asProvider_returnsForbidden() throws Exception {
//        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("GET /admin/dashboard should return 200 for admin users")
//    @WithMockUser(roles = "ADMIN")
//    void dashboard_asAdmin_returns200() throws Exception {
//        when(userRepository.findAll()).thenReturn(List.of());
//        when(appointmentService.getAllAppointments()).thenReturn(List.of());
//
//        mockMvc.perform(get("/admin/dashboard"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/dashboard"));
//    }
//
//    @Test
//    @DisplayName("GET /admin/providers should list all providers")
//    @WithMockUser(roles = "ADMIN")
//    void providers_returnsProvidersView() throws Exception {
//        when(userRepository.findByRole(Role.ROLE_PROVIDER)).thenReturn(List.of());
//
//        mockMvc.perform(get("/admin/providers"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/providers"))
//                .andExpect(model().attributeExists("providers"));
//    }
//
//    @Test
//    @DisplayName("POST /admin/providers/{id}/approve should redirect on success")
//    @WithMockUser(roles = "ADMIN")
//    void approveProvider_existingUser_redirects() throws Exception {
//        User user = adminTestUser(Role.ROLE_PROVIDER);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        mockMvc.perform(post("/admin/providers/1/approve").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/admin/providers"));
//
//        verify(userRepository).save(user);
//        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
//    }
//
//    @Test
//    @DisplayName("POST /admin/providers/{id}/approve should show error when user not found")
//    @WithMockUser(roles = "ADMIN")
//    void approveProvider_notFound_redirectsWithError() throws Exception {
//        when(userRepository.findById(99L)).thenThrow(new EntityNotFoundException("User not found"));
//
//        mockMvc.perform(post("/admin/providers/99/approve").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /admin/providers/{id}/suspend should set account to SUSPENDED")
//    @WithMockUser(roles = "ADMIN")
//    void suspendProvider_existingUser_setsSuspended() throws Exception {
//        User user = adminTestUser(Role.ROLE_PROVIDER);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        mockMvc.perform(post("/admin/providers/1/suspend").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/admin/providers"));
//
//        assertEquals(AccountStatus.SUSPENDED, user.getAccountStatus());
//    }
//
//    @Test
//    @DisplayName("GET /admin/clients should list all clients")
//    @WithMockUser(roles = "ADMIN")
//    void clients_returnsClientsView() throws Exception {
//        when(userRepository.findByRole(Role.ROLE_CLIENT)).thenReturn(List.of());
//
//        mockMvc.perform(get("/admin/clients"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/clients"))
//                .andExpect(model().attributeExists("clients"));
//    }
//
//    @Test
//    @DisplayName("GET /admin/users should list all users")
//    @WithMockUser(roles = "ADMIN")
//    void users_returnsUsersView() throws Exception {
//        when(userRepository.findAll()).thenReturn(List.of());
//
//        mockMvc.perform(get("/admin/users"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/users"))
//                .andExpect(model().attributeExists("users"));
//    }
//
//    @Test
//    @DisplayName("POST /admin/users/{id}/deactivate should set account to SUSPENDED")
//    @WithMockUser(roles = "ADMIN")
//    void deactivateUser_existingUser_setsSuspended() throws Exception {
//        User user = adminTestUser(Role.ROLE_CLIENT);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        mockMvc.perform(post("/admin/users/1/deactivate").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/admin/users"));
//
//        assertEquals(AccountStatus.SUSPENDED, user.getAccountStatus());
//        verify(userRepository).save(user);
//    }
//
//    @Test
//    @DisplayName("POST /admin/users/{id}/deactivate should show error when user not found")
//    @WithMockUser(roles = "ADMIN")
//    void deactivateUser_notFound_redirectsWithError() throws Exception {
//        when(userRepository.findById(99L)).thenThrow(new EntityNotFoundException("User not found"));
//
//        mockMvc.perform(post("/admin/users/99/deactivate").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("GET /admin/appointments should list all appointments")
//    @WithMockUser(roles = "ADMIN")
//    void appointments_noFilter_returnsAll() throws Exception {
//        AppointmentResponse appointmentResponse = new AppointmentResponse();
//        appointmentResponse.setId(1L);
//        appointmentResponse.setStatus(AppointmentStatus.CONFIRMED);
//        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointmentResponse));
//
//        mockMvc.perform(get("/admin/appointments"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/appointments"))
//                .andExpect(model().attributeExists("appointments"));
//    }
//
//    @Test
//    @DisplayName("GET /admin/appointments with status filter should return filtered results")
//    @WithMockUser(roles = "ADMIN")
//    void appointments_withStatusFilter_returnsFiltered() throws Exception {
//        AppointmentResponse appointmentResponse = new AppointmentResponse();
//        appointmentResponse.setId(1L);
//        appointmentResponse.setStatus(AppointmentStatus.REQUESTED);
//        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointmentResponse));
//
//        mockMvc.perform(get("/admin/appointments").param("status", "REQUESTED"))
//                .andExpect(status().isOk())
//                .andExpect(model().attributeExists("appointments"));
//    }
//
//    @Test
//    @DisplayName("GET /admin/services should list all services")
//    @WithMockUser(roles = "ADMIN")
//    void services_returnsServicesView() throws Exception {
//        when(providerServiceOfferingRepository.findAll()).thenReturn(List.of());
//
//        mockMvc.perform(get("/admin/services"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/services"))
//                .andExpect(model().attributeExists("services"));
//    }
//
//    @Test
//    @DisplayName("POST /admin/services/{id}/deactivate should redirect on success")
//    @WithMockUser(roles = "ADMIN")
//    void deactivateService_existing_redirects() throws Exception {
//        mockMvc.perform(post("/admin/services/1/deactivate").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/admin/services"));
//    }
//
//    @Test
//    @DisplayName("POST /admin/services/{id}/activate should redirect on success")
//    @WithMockUser(roles = "ADMIN")
//    void activateService_existing_redirects() throws Exception {
//        mockMvc.perform(post("/admin/services/1/activate").with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/admin/services"));
//    }
}