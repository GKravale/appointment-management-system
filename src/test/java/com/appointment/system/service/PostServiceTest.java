package com.appointment.system.service;

import com.appointment.system.entity.*;
import com.appointment.system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private MediaAssetRepository mediaAssetRepository;
    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PostService postService;

    private Provider provider;
    private Portfolio portfolio;
    private Post post;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postService, "uploadDir", System.getProperty("java.io.tmpdir"));

        provider = new Provider();
        provider.setId(1L);
        provider.setFirstName("Līga");
        provider.setLastName("Ozola");

        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setProvider(provider);
        portfolio.setTitle("Līga's Portfolio");

        post = new Post();
        post.setId(1L);
        post.setProvider(provider);
        post.setContentText("My latest work");
        post.setIsDeleted(false);
        post.setPostMediaList(new ArrayList<>());
    }

    @Test
    @DisplayName("Should return active posts for existing provider")
    void getProviderPosts_existingProvider_returnsList() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(postRepository.findByProviderAndIsDeletedFalse(provider)).thenReturn(List.of(post));

        List<Post> result = postService.getProviderPosts(1L);

        assertEquals(1, result.size());
        assertEquals("My latest work", result.getFirst().getContentText());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on getProviderPosts")
    void getProviderPosts_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> postService.getProviderPosts(99L));
    }

    @Test
    @DisplayName("Should return post when found by ID")
    void getById_existingPost_returnsPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when post not found by ID")
    void getById_notFound_throws() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> postService.getById(99L));
    }

    @Test
    @DisplayName("Should return only the requested number of recent posts")
    void getAllRecentPosts_limitsResults() {
        Post post1 = new Post();
        post1.setIsDeleted(false);
        Post post2 = new Post();
        post2.setIsDeleted(false);
        Post post3 = new Post();
        post3.setIsDeleted(false);

        when(postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(post1, post2, post3));

        List<Post> result = postService.getAllRecentPosts(2);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return all posts when limit is larger than total")
    void getAllRecentPosts_limitLargerThanTotal_returnsAll() {
        when(postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(post));

        List<Post> result = postService.getAllRecentPosts(100);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should create post with text only and no media")
    void createPost_textOnly_savesPost() throws IOException {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.createPost(1L, "My post", null, null, false);

        verify(postRepository, times(2)).save(any(Post.class));
        verify(mediaAssetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create post and attach uploaded image file")
    void createPost_withImageFile_attachesMedia() throws IOException {
        MockMultipartFile file = new MockMultipartFile("files", "photo.jpg", "image/jpeg", new byte[100]);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post post = inv.getArgument(0);
            post.setId(1L);
            return post;
        });

        postService.createPost(1L, "New work!", List.of(file), null, false);

        verify(mediaAssetRepository, atLeastOnce()).save(any(MediaAsset.class));
    }

    @Test
    @DisplayName("Should skip unsupported file types when creating post")
    void createPost_unsupportedFileType_skipsFile() throws IOException {
        MockMultipartFile pdfFile = new MockMultipartFile("files", "doc.pdf", "application/pdf", new byte[100]);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.createPost(1L, "Post with pdf", List.of(pdfFile), null, false);

        verify(mediaAssetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should attach existing portfolio asset to post")
    void createPost_withPortfolioAssetId_attachesExistingAsset() throws IOException {
        MediaAsset existingAsset = new MediaAsset();
        existingAsset.setId(5L);
        existingAsset.setPortfolio(portfolio);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mediaAssetRepository.findById(5L)).thenReturn(Optional.of(existingAsset));

        postService.createPost(1L, "Reusing asset", null, List.of(5L), false);

        verify(mediaAssetRepository).findById(5L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on createPost")
    void createPost_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                postService.createPost(99L, "text", null, null, false));
    }

    @Test
    @DisplayName("Should soft-delete post when provider owns it")
    void deletePost_owner_setsDeletedTrue() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, 1L);

        assertTrue(post.getIsDeleted());
    }

    @Test
    @DisplayName("Should throw SecurityException when different provider tries to delete post")
    void deletePost_notOwner_throwsSecurityException() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(SecurityException.class, () -> postService.deletePost(1L, 99L));
        assertFalse(post.getIsDeleted());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when post not found on delete")
    void deletePost_notFound_throws() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> postService.deletePost(99L, 1L));
    }
}