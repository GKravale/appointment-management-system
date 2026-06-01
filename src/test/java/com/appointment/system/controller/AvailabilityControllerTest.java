package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.BlockType;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.AvailabilityService;
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

import java.time.DayOfWeek;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvailabilityController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;
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
//    @DisplayName("GET /provider/availability should return availability page with model")
//    void availabilityPage_authenticated_returnsView() throws Exception {
//        when(availabilityService.getAvailability(anyLong())).thenReturn(List.of());
//        when(availabilityService.getAllDays()).thenReturn(List.of(DayOfWeek.values()));
//        when(timeBlockService.getTimeBlocks(anyLong())).thenReturn(List.of());
//        when(timeBlockService.getAllBlockTypes()).thenReturn(List.of(BlockType.values()));
//
//        mockMvc.perform(get("/provider/availability").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/availability"))
//                .andExpect(model().attributeExists("availabilities"))
//                .andExpect(model().attributeExists("createRequest"))
//                .andExpect(model().attributeExists("days"))
//                .andExpect(model().attributeExists("timeBlocks"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/availability/create should redirect on success")
//    void create_validRequest_redirects() throws Exception {
//        doNothing().when(availabilityService).create(anyLong(), any());
//
//        mockMvc.perform(post("/provider/availability/create").with(user(providerUser)).with(csrf())
//                        .param("dayOfWeek", "MONDAY")
//                        .param("startTime", "09:00")
//                        .param("endTime", "17:00"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/availability"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/availability/create should show error on overlapping slot")
//    void create_overlappingSlot_redirectsWithError() throws Exception {
//        doThrow(new IllegalArgumentException("Overlapping availability"))
//                .when(availabilityService).create(anyLong(), any());
//
//        mockMvc.perform(post("/provider/availability/create").with(user(providerUser)).with(csrf())
//                        .param("dayOfWeek", "MONDAY")
//                        .param("startTime", "10:00")
//                        .param("endTime", "14:00"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/availability/delete/{id} should redirect on success")
//    void delete_ownerDeletes_redirects() throws Exception {
//        doNothing().when(availabilityService).delete(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/availability/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/availability"));
//    }
}