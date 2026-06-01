package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.entity.Post;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import com.appointment.system.service.PortfolioService;
import com.appointment.system.service.PostService;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostService postService;
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
//    @DisplayName("GET /provider/posts should return posts page with posts and portfolio assets")
//    void postsPage_authenticated_returnsView() throws Exception {
//        when(postService.getProviderPosts(1L)).thenReturn(List.of());
//        when(portfolioService.getPortfolioAssets(1L)).thenReturn(List.of());
//
//        mockMvc.perform(get("/provider/posts").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/posts"))
//                .andExpect(model().attributeExists("posts"))
//                .andExpect(model().attributeExists("portfolioAssets"));
//    }
//
//    @Test
//    @DisplayName("GET /provider/posts/{id} should return post detail view")
//    void viewPost_existingPost_returnsDetailView() throws Exception {
//        Post post = new Post();
//        post.setId(1L);
//        when(postService.getById(1L)).thenReturn(post);
//
//        mockMvc.perform(get("/provider/posts/1").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/post-detail"))
//                .andExpect(model().attributeExists("post"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/posts/create should redirect on successful post creation")
//    void createPost_validRequest_redirectsWithSuccess() throws Exception {
//        doNothing().when(postService).createPost(anyLong(), any(), any(), any(), anyBoolean());
//
//        mockMvc.perform(post("/provider/posts/create").with(user(providerUser)).with(csrf())
//                        .param("contentText", "New post content"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/posts"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/posts/create should show error when provider not found")
//    void createPost_providerNotFound_redirectsWithError() throws Exception {
//        doThrow(new EntityNotFoundException("Provider not found"))
//                .when(postService).createPost(anyLong(), any(), any(), any(), anyBoolean());
//
//        mockMvc.perform(post("/provider/posts/create").with(user(providerUser)).with(csrf())
//                        .param("contentText", "Content"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/posts/delete/{id} should redirect on successful delete")
//    void deletePost_owner_redirectsWithSuccess() throws Exception {
//        doNothing().when(postService).deletePost(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/posts/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/posts"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/posts/delete/{id} should propagate SecurityException when not owner")
//    void deletePost_notOwner_throws() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(postService).deletePost(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/posts/delete/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is5xxServerError());
//    }
}
