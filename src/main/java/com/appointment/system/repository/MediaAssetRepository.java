package com.appointment.system.repository;

import com.appointment.system.entity.MediaAsset;
import com.appointment.system.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    List<MediaAsset> findByPortfolioAndInPortfolioTrue(Portfolio portfolio);
}
