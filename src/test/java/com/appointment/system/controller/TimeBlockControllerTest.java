package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import com.appointment.system.service.TimeBlockService;
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

@WebMvcTest(TimeBlockController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class TimeBlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeBlockService timeBlockService;
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
//    @DisplayName("POST /provider/timeblocks/create should redirect to availability on success")
//    void create_validTimeBlock_redirects() throws Exception {
//        doNothing().when(timeBlockService).create(anyLong(), any(), any(), any(), any());
//
//        mockMvc.perform(post("/provider/timeblocks/create").with(user(providerUser)).with(csrf())
//                        .param("startDateTime", "2025-06-10T10:00:00")
//                        .param("endDateTime", "2025-06-10T11:00:00")
//                        .param("type", "BREAK"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/availability"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/timeblocks/create should show error when start is after end")
//    void create_startAfterEnd_redirectsWithError() throws Exception {
//        doThrow(new IllegalArgumentException("Start must be before end"))
//                .when(timeBlockService).create(anyLong(), any(), any(), any(), any());
//
//        mockMvc.perform(post("/provider/timeblocks/create").with(user(providerUser)).with(csrf())
//                        .param("startDateTime", "2025-06-10T14:00:00")
//                        .param("endDateTime", "2025-06-10T10:00:00")
//                        .param("type", "BREAK"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/timeblocks/delete/{id} should redirect on success")
//    void delete_ownerDeletes_redirects() throws Exception {
//        doNothing().when(timeBlockService).delete(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/timeblocks/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/availability"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/timeblocks/delete/{id} should still redirect when not authorized")
//    void delete_notOwner_redirects() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(timeBlockService).delete(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/timeblocks/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is5xxServerError());
//    }
}