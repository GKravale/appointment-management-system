package com.appointment.system.service;

import com.appointment.system.entity.*;
import com.appointment.system.enums.MediaType;
import com.appointment.system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final ProviderRepository providerRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml");

    public Portfolio getOrCreatePortfolio(Long providerPersonId) {
        log.debug("Getting or creating portfolio for provider {}", providerPersonId);
        Provider provider = providerRepository.findById(providerPersonId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        return portfolioRepository.findByProvider(provider).orElseGet(() -> {
            Portfolio p = new Portfolio();
            p.setProvider(provider);
            p.setTitle(provider.getFirstName() + "'s Portfolio");
            return portfolioRepository.save(p);
        });
    }

    public List<MediaAsset> getPortfolioAssets(Long providerPersonId) {
        log.debug("Retrieving portfolio assets for provider {}", providerPersonId);
        Portfolio portfolio = getOrCreatePortfolio(providerPersonId);
        return mediaAssetRepository.findByPortfolioAndInPortfolioTrue(portfolio);
    }

    @Transactional
    public void uploadMedia(Long providerPersonId, MultipartFile file, String altText) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, WebP, GIF and SVG images are allowed in portfolio");
        }

        Portfolio portfolio = getOrCreatePortfolio(providerPersonId);

        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(uploadDir, "portfolio");
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        MediaAsset asset = new MediaAsset();
        asset.setPortfolio(portfolio);
        asset.setFilePath("uploads/portfolio/" + filename);
        asset.setMediaType(MediaType.IMAGE);
        asset.setAltText(altText);
        asset.setInPortfolio(true);

        log.info("Portfolio image uploaded for provider {}: {}", providerPersonId, filename);
        mediaAssetRepository.save(asset);
    }

    @Transactional
    public void deleteMedia(Long mediaAssetId, Long providerPersonId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new EntityNotFoundException("Media not found"));
        if (!asset.getPortfolio().getProvider().getId().equals(providerPersonId)) {
            throw new SecurityException("Not authorized");
        }
        try {
            Files.deleteIfExists(Paths.get(asset.getFilePath()));
        } catch (IOException e) {
            log.warn("Could not delete file: {}", asset.getFilePath());
        }
        mediaAssetRepository.delete(asset);
        log.info("Portfolio media deleted: id={}, provider={}", mediaAssetId, providerPersonId);
    }
}