package com.appointment.system.repository;

import com.appointment.system.entity.Appointment;
import com.appointment.system.entity.Client;
import com.appointment.system.entity.Provider;
import com.appointment.system.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClient(Client client);

    List<Appointment> findByProvider(Provider provider);

    List<Appointment> findByProviderAndStatus(Provider provider, AppointmentStatus status);

    List<Appointment> findByClientAndStatus(Client client, AppointmentStatus status);
}