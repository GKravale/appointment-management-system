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
public class PostService {

    private final PostRepository postRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final ProviderRepository providerRepository;
    private final PortfolioRepository portfolioRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif",
            "image/svg+xml", "video/mp4", "video/webm", "video/ogg");

    public List<Post> getProviderPosts(Long providerPersonId) {
        Provider provider =
                providerRepository.findById(providerPersonId).orElseThrow(() -> new EntityNotFoundException("Provider" +
                        " not found"));
        log.info("Retrieving posts for provider {}", providerPersonId);
        return postRepository.findByProviderAndIsDeletedFalse(provider);
    }

    public Post getById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    public List<Post> getAllRecentPosts(int limit) {
        return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream().limit(limit).toList();
    }

    @Transactional
    public void createPost(Long providerPersonId, String contentText, List<MultipartFile> files,
                           List<Long> portfolioAssetIds, boolean addToPortfolio) throws IOException {

        Provider provider =
                providerRepository.findById(providerPersonId).orElseThrow(() -> new EntityNotFoundException("Provider" +
                        " not found"));

        Post post = new Post();
        post.setProvider(provider);
        post.setContentText(contentText);
        post.setPostMediaList(new ArrayList<>());
        postRepository.save(post);

        Portfolio portfolio = getOrCreatePortfolio(provider);

        if (files != null) {
            attachUploadedFiles(post, files, portfolio, addToPortfolio);
        }

        if (portfolioAssetIds != null) {
            attachPortfolioAssets(post, portfolioAssetIds);
        }

        postRepository.save(post);
        log.info("Post created by provider {}", providerPersonId);
    }

    @Transactional
    public void deletePost(Long postId, Long providerPersonId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
        if (!post.getProvider().getId().equals(providerPersonId)) {
            throw new SecurityException("Not authorized");
        }
        post.setIsDeleted(true);
        log.info("Post {} soft-deleted by provider {}", postId, providerPersonId);
    }


    private void attachUploadedFiles(Post post, List<MultipartFile> files, Portfolio portfolio,
                                     boolean addToPortfolio) throws IOException {
        int order = post.getPostMediaList().size();
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (file.isEmpty() || !isSupportedMediaType(contentType)) {
                continue;
            }
            MediaAsset asset = saveFileAndCreateAsset(file, contentType, portfolio);
            if (addToPortfolio && contentType.startsWith("image/")) {
                asset.setPortfolio(portfolio);
            }
            mediaAssetRepository.save(asset);

            PostMedia postMedia = new PostMedia();
            postMedia.setPost(post);
            postMedia.setMediaAsset(asset);
            postMedia.setSortOrder(order++);
            post.getPostMediaList().add(postMedia);
        }
    }

    private MediaAsset saveFileAndCreateAsset(MultipartFile file, String contentType, Portfolio portfolio) throws IOException {
        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(uploadDir, "posts");
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        MediaAsset asset = new MediaAsset();
        asset.setPortfolio(portfolio);
        asset.setFilePath("uploads/posts/" + filename);
        asset.setMediaType(contentType.startsWith("video/") ? MediaType.VIDEO : MediaType.IMAGE);
        return asset;
    }

    private void attachPortfolioAssets(Post post, List<Long> portfolioAssetIds) {
        for (Long assetId : portfolioAssetIds) {
            mediaAssetRepository.findById(assetId).ifPresent(asset -> {
                PostMedia postMedia = new PostMedia();
                postMedia.setPost(post);
                postMedia.setMediaAsset(asset);
                postMedia.setSortOrder(post.getPostMediaList().size());
                post.getPostMediaList().add(postMedia);
            });
        }
    }

    private Portfolio getOrCreatePortfolio(Provider provider) {
        return portfolioRepository.findByProvider(provider).orElseGet(() -> {
            Portfolio portfolio = new Portfolio();
            portfolio.setProvider(provider);
            portfolio.setTitle(provider.getFirstName() + "'s Portfolio");
            return portfolioRepository.save(portfolio);
        });
    }

    private boolean isSupportedMediaType(String contentType) {
        return contentType != null && ALLOWED_TYPES.contains(contentType);
    }
}