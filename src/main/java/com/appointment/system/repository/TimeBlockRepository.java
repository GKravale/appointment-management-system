package com.appointment.system.repository;

import com.appointment.system.entity.Provider;
import com.appointment.system.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    List<TimeBlock> findByProvider(Provider provider);

    List<TimeBlock> findByProviderAndStartDateTimeBetween(Provider provider, LocalDateTime from, LocalDateTime to);
}
