package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.entity.Post;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.service.NotificationService;
import com.appointment.system.service.PortfolioService;
import com.appointment.system.service.PostService;
import com.appointment.system.service.ProviderService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProvidersController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class ProvidersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderService providerService;
    @MockitoBean
    private PortfolioService portfolioService;
    @MockitoBean
    private PostService postService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

//    @Test
//    @DisplayName("GET /providers should return browse page with all providers")
//    void browseProviders_noFilter_returnsAllProviders() throws Exception {
//        ProviderProfileResponse response = new ProviderProfileResponse();
//        response.setId(1L);
//        response.setFirstName("Līga");
//        response.setLastName("Ozola");
//
//        when(providerService.getAllActiveProviders()).thenReturn(List.of(response));
//
//        mockMvc.perform(get("/providers"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("providers/browse"))
//                .andExpect(model().attributeExists("providers"))
//                .andExpect(model().attributeExists("categories"));
//    }
//
//    @Test
//    @DisplayName("GET /providers?category=HAIR should return filtered providers")
//    void browseProviders_withCategoryFilter_returnsFiltered() throws Exception {
//        ProviderProfileResponse response = new ProviderProfileResponse();
//        response.setId(1L);
//        response.setFirstName("Līga");
//
//        when(providerService.getActiveProvidersByCategory(ServiceCategory.HAIR)).thenReturn(List.of(response));
//
//        mockMvc.perform(get("/providers").param("category", "HAIR"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("providers/browse"))
//                .andExpect(model().attribute("selectedCategory", ServiceCategory.HAIR))
//                .andExpect(model().attributeExists("providers"));
//    }
//
//    @Test
//    @DisplayName("GET /providers should return empty list when no providers match category")
//    void browseProviders_noMatchingCategory_returnsEmptyList() throws Exception {
//        when(providerService.getActiveProvidersByCategory(ServiceCategory.TATTOO)).thenReturn(List.of());
//
//        mockMvc.perform(get("/providers").param("category", "TATTOO"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("providers/browse"))
//                .andExpect(model().attributeExists("providers"));
//    }
//
//    @Test
//    @DisplayName("GET /providers/{id} should return provider profile with posts and portfolio")
//    void providerProfile_existingProvider_returnsProfileView() throws Exception {
//        ProviderProfileResponse profile = new ProviderProfileResponse();
//        profile.setId(1L);
//        profile.setFirstName("Līga");
//
//        when(providerService.getPublicProfile(1L)).thenReturn(profile);
//        when(portfolioService.getPortfolioAssets(1L)).thenReturn(List.of());
//        when(postService.getProviderPosts(1L)).thenReturn(List.of());
//
//        mockMvc.perform(get("/providers/1"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("providers/profile"))
//                .andExpect(model().attributeExists("provider"))
//                .andExpect(model().attributeExists("assets"))
//                .andExpect(model().attributeExists("posts"))
//                .andExpect(model().attributeExists("totalPosts"));
//    }
//
//    @Test
//    @DisplayName("GET /providers/{id} should propagate exception when provider not found")
//    void providerProfile_notFound_throws() throws Exception {
//        when(providerService.getPublicProfile(99L)).thenThrow(new EntityNotFoundException("Provider not found"));
//
//        mockMvc.perform(get("/providers/99")).andExpect(status().is5xxServerError());
//    }
//
//    @Test
//    @DisplayName("GET /providers/{id} totalPosts count should match post list size")
//    void providerProfile_totalPostsMatchesListSize() throws Exception {
//        ProviderProfileResponse profile = new ProviderProfileResponse();
//        profile.setId(1L);
//
//        when(providerService.getPublicProfile(1L)).thenReturn(profile);
//        when(portfolioService.getPortfolioAssets(1L)).thenReturn(List.of());
//
//        Post post1 = new Post();
//        Post post2 = new Post();
//        when(postService.getProviderPosts(1L)).thenReturn(List.of(post1, post2));
//
//        mockMvc.perform(get("/providers/1")).andExpect(model().attribute("totalPosts", 2));
//    }
}