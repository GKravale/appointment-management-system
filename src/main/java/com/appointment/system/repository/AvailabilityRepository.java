package com.appointment.system.repository;

import com.appointment.system.entity.Availability;
import com.appointment.system.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByProvider(Provider provider);

    List<Availability> findByProviderAndDayOfWeek(Provider provider, DayOfWeek dayOfWeek);
}
