package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.entity.Client;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import com.appointment.system.entity.User;
import com.appointment.system.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails currentUser;

    @BeforeEach
    void setUp() {
        Client person = new Client();
        person.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setUsername("user@test.com");
        user.setEmail("user@test.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_CLIENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPerson(person);

        currentUser = new CustomUserDetails(user);
    }

//    @Test
//    @DisplayName("GET /notifications should mark all read and return notifications view")
//    void notificationsPage_authenticated_marksAllReadAndReturnsView() throws Exception {
//        User user = new User();
//        user.setId(1L);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        doNothing().when(notificationService).markAllRead(1L);
//        when(notificationService.getForUser(user)).thenReturn(List.of());
//
//        mockMvc.perform(get("/notifications").with(user(currentUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("notifications"))
//                .andExpect(model().attributeExists("notifications"));
//
//        verify(notificationService).markAllRead(1L);
//    }
//
//    @Test
//    @DisplayName("POST /notifications/read/{id} should return 200 and mark notification read")
//    void markRead_existingNotification_returns200() throws Exception {
//        doNothing().when(notificationService).markRead(1L);
//
//        mockMvc.perform(post("/notifications/read/1").with(user(currentUser)).with(csrf()))
//                .andExpect(status().isOk());
//
//        verify(notificationService).markRead(1L);
//    }
}
