package com.appointment.system.repository;

import com.appointment.system.entity.Appointment;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.Provider;
import com.appointment.system.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClientOrderByStartTimeDesc(Client client);

    List<Appointment> findByProviderOrderByStartTimeDesc(Provider provider);

    List<Appointment> findAllByOrderByStartTimeDesc();

    List<Appointment> findByProviderAndStatusOrderByStartTimeDesc(Provider provider, AppointmentStatus status);

    List<Appointment> findByClientAndStatusOrderByStartTimeDesc(Client client, AppointmentStatus status);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.client = :client " +
            "AND a.startTime = :startTime " +
            "AND a.status NOT IN (com.appointment.system.enums.AppointmentStatus.CANCELLED, " +
            "com.appointment.system.enums.AppointmentStatus.DECLINED)")
    boolean existsActiveBookingForClientAtTime(@Param("client") Client client,
                                               @Param("startTime") LocalDateTime startTime);
}