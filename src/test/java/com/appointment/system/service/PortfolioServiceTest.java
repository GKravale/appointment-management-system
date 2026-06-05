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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private MediaAssetRepository mediaAssetRepository;
    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Provider provider;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(portfolioService, "uploadDir", System.getProperty("java.io.tmpdir"));

        provider = new Provider();
        provider.setId(1L);
        provider.setFirstName("Līga");
        provider.setLastName("Ozola");

        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setProvider(provider);
        portfolio.setTitle("Līga's Portfolio");
    }

    @Test
    @DisplayName("Should return existing portfolio when one already exists")
    void getOrCreatePortfolio_existing_returnsExisting() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));

        Portfolio result = portfolioService.getOrCreatePortfolio(1L);

        assertEquals(portfolio, result);
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create and save new portfolio when none exists")
    void getOrCreatePortfolio_notExisting_createsNew() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.empty());
        when(portfolioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Portfolio result = portfolioService.getOrCreatePortfolio(1L);

        assertNotNull(result);
        assertEquals("Līga's Portfolio", result.getTitle());
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found")
    void getOrCreatePortfolio_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> portfolioService.getOrCreatePortfolio(99L));
    }

    @Test
    @DisplayName("Should return list of media assets in portfolio")
    void getPortfolioAssets_returnsAssets() {
        MediaAsset asset = new MediaAsset();
        asset.setFilePath("uploads/portfolio/test.jpg");

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));
        when(mediaAssetRepository.findByPortfolioAndInPortfolioTrue(portfolio)).thenReturn(List.of(asset));

        List<MediaAsset> result = portfolioService.getPortfolioAssets(1L);

        assertEquals(1, result.size());
        assertEquals("uploads/portfolio/test.jpg", result.getFirst().getFilePath());
    }

    @Test
    @DisplayName("Should return empty list when portfolio has no assets")
    void getPortfolioAssets_noAssets_returnsEmpty() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));
        when(mediaAssetRepository.findByPortfolioAndInPortfolioTrue(portfolio)).thenReturn(List.of());

        List<MediaAsset> result = portfolioService.getPortfolioAssets(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should upload valid image and save media asset")
    void uploadMedia_validImage_savesAsset() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[100]);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));

        portfolioService.uploadMedia(1L, file, "My work");

        verify(mediaAssetRepository).save(argThat(asset -> asset.getFilePath().startsWith("uploads/portfolio/")
                && asset.getFilePath().endsWith(".jpg") && "My work".equals(asset.getAltText())
                && Boolean.TRUE.equals(asset.getInPortfolio())));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when file is empty")
    void uploadMedia_emptyFile_throws() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> portfolioService.uploadMedia(1L, emptyFile, null));
        verify(mediaAssetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for disallowed file type (PDF)")
    void uploadMedia_pdfFile_throws() {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[100]);

        assertThrows(IllegalArgumentException.class, () -> portfolioService.uploadMedia(1L, pdfFile, null));
        verify(mediaAssetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for disallowed file type (video)")
    void uploadMedia_videoFile_throws() {
        MockMultipartFile videoFile = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[100]);

        assertThrows(IllegalArgumentException.class, () -> portfolioService.uploadMedia(1L, videoFile, null));
    }

    @Test
    @DisplayName("Should accept WebP image format")
    void uploadMedia_webpFile_savesSuccessfully() throws IOException {
        MockMultipartFile webpFile = new MockMultipartFile("file", "image.webp", "image/webp", new byte[100]);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(portfolioRepository.findByProvider(provider)).thenReturn(Optional.of(portfolio));

        portfolioService.uploadMedia(1L, webpFile, "Webp image");

        verify(mediaAssetRepository).save(argThat(asset -> asset.getFilePath().endsWith(".webp")));
    }

    @Test
    @DisplayName("Should delete media asset when provider owns it")
    void deleteMedia_ownerDeletes_removesAsset() {
        MediaAsset asset = new MediaAsset();
        asset.setId(10L);
        asset.setPortfolio(portfolio);
        asset.setFilePath("uploads/portfolio/some-nonexistent-file.jpg");

        when(mediaAssetRepository.findById(10L)).thenReturn(Optional.of(asset));

        portfolioService.deleteMedia(10L, 1L);

        verify(mediaAssetRepository).delete(asset);
    }

    @Test
    @DisplayName("Should throw SecurityException when different provider tries to delete")
    void deleteMedia_notOwner_throwsSecurityException() {
        MediaAsset asset = new MediaAsset();
        asset.setPortfolio(portfolio);

        when(mediaAssetRepository.findById(10L)).thenReturn(Optional.of(asset));

        assertThrows(SecurityException.class, () -> portfolioService.deleteMedia(10L, 99L));
        verify(mediaAssetRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when media asset not found")
    void deleteMedia_assetNotFound_throws() {
        when(mediaAssetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> portfolioService.deleteMedia(99L, 1L));
    }
}