package com.appointment.system.repository;

import com.appointment.system.entity.Provider;
import com.appointment.system.entity.ProviderServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderServiceOfferingRepository extends JpaRepository<ProviderServiceOffering, Long> {

    List<ProviderServiceOffering> findByProviderAndIsActiveTrueAndIsDeletedFalse(Provider provider);
}
