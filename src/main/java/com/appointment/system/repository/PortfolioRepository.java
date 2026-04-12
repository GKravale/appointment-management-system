package com.appointment.system.repository;

import com.appointment.system.entity.Portfolio;
import com.appointment.system.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByProvider(Provider provider);
}