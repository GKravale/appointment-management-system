package com.appointment.system.repository;

import com.appointment.system.entity.ServiceOffering;
import com.appointment.system.enums.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    List<ServiceOffering> findByIsActiveTrueAndIsDeletedFalse();

    List<ServiceOffering> findByCategoryAndIsActiveTrueAndIsDeletedFalse(ServiceCategory category);
}
