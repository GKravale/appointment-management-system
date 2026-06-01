package com.appointment.system.entity;

import com.appointment.system.enums.MediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "media_asset")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    @ToString.Exclude
    private Portfolio portfolio;

    @NotNull
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "in_portfolio", nullable = false)
    private Boolean inPortfolio = false;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "mediaAsset", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PostMedia> postMediaList = new ArrayList<>();

    public MediaAsset(Portfolio portfolio, String filePath, MediaType mediaType, String altText, Boolean inPortfolio) {
        this.portfolio = portfolio;
        this.filePath = filePath;
        this.mediaType = mediaType;
        this.altText = altText;
        this.inPortfolio = inPortfolio;
    }

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }
}
