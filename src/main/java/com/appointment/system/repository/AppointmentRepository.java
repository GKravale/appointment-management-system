package com.appointment.system.repository;

import com.appointment.system.entity.Appointment;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.Provider;
import com.appointment.system.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClientOrderByStartTimeDesc(Client client);

    List<Appointment> findByProviderOrderByStartTimeDesc(Provider provider);

    List<Appointment> findAllByOrderByStartTimeDesc();

    List<Appointment> findByProviderAndStatusOrderByStartTimeDesc(Provider provider, AppointmentStatus status);

    List<Appointment> findByClientAndStatusOrderByStartTimeDesc(Client client, AppointmentStatus status);
}