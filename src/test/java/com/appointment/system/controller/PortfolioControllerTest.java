package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.entity.MediaAsset;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import com.appointment.system.service.PortfolioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PortfolioService portfolioService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails providerUser;

    @BeforeEach
    void setUp() {
        Provider person = new Provider();
        person.setId(1L);

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
//    @DisplayName("GET /provider/portfolio should return portfolio page with assets")
//    void portfolioPage_authenticated_returnsView() throws Exception {
//        when(portfolioService.getPortfolioAssets(1L)).thenReturn(List.of(new MediaAsset()));
//
//        mockMvc.perform(get("/provider/portfolio").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/portfolio"))
//                .andExpect(model().attributeExists("assets"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/portfolio/upload should redirect on successful upload")
//    void uploadMedia_validImage_redirectsWithSuccess() throws Exception {
//        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[100]);
//
//        doNothing().when(portfolioService).uploadMedia(anyLong(), any(), any());
//
//        mockMvc.perform(multipart("/provider/portfolio/upload").file(file).param("altText", "My work")
//                        .with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/portfolio"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/portfolio/upload should show error for disallowed file type")
//    void uploadMedia_invalidFileType_redirectsWithError() throws Exception {
//        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[100]);
//
//        doThrow(new IllegalArgumentException("Only image files are allowed"))
//                .when(portfolioService).uploadMedia(anyLong(), any(), any());
//
//        mockMvc.perform(multipart("/provider/portfolio/upload").file(file).with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/portfolio/delete/{id} should redirect on successful delete")
//    void deleteMedia_owner_redirectsWithSuccess() throws Exception {
//        doNothing().when(portfolioService).deleteMedia(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/portfolio/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/portfolio"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/portfolio/delete/{id} should show error when not authorized")
//    void deleteMedia_notOwner_redirectsWithError() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(portfolioService).deleteMedia(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/portfolio/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/portfolio/delete/{id} should show error when asset not found")
//    void deleteMedia_notFound_redirectsWithError() throws Exception {
//        doThrow(new EntityNotFoundException("Media not found")).when(portfolioService).deleteMedia(anyLong(),
//                anyLong());
//
//        mockMvc.perform(post("/provider/portfolio/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
}
