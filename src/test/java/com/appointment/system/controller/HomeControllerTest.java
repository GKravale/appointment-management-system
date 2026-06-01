package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.service.NotificationService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class HomeControllerTest {

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

//    @Test
//    @DisplayName("GET / should return home page with category list")
//    void home_returnsHomeView() throws Exception {
//        mockMvc.perform(get("/"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("home"))
//                .andExpect(model().attributeExists("categoryIcons"));
//    }
//
//    @Test
//    @DisplayName("GET / should include all ServiceCategory values in model")
//    void home_containsAllCategories() throws Exception {
//        mockMvc.perform(get("/"))
//                .andExpect(status().isOk())
//                .andExpect(model().attribute("categoryIcons", List.of(ServiceCategory.values())));
//    }
//
//    @Test
//    @DisplayName("GET /privacy should return privacy policy page")
//    void privacyPolicy_returnsPrivacyView() throws Exception {
//        mockMvc.perform(get("/privacy"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("legal/privacy"));
//    }
//
//    @Test
//    @DisplayName("GET /terms should return terms of service page")
//    void termsOfService_returnsTermsView() throws Exception {
//        mockMvc.perform(get("/terms"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("legal/terms"));
//    }
}
