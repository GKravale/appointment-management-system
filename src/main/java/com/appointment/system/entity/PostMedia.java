package com.appointment.system.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @ToString.Exclude
    private Post post;

    @ManyToOne
    @JoinColumn(name = "media_asset_id", nullable = false)
    @ToString.Exclude
    private MediaAsset mediaAsset;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
