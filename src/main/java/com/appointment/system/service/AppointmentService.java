package com.appointment.system.service;

import com.appointment.system.dto.request.BookAppointmentRequest;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.entity.*;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.repository.*;
import com.appointment.system.util.TimeSlotCalculator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ProviderServiceOfferingRepository providerServiceOfferingRepository;
    private final ProviderRepository providerRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;
    private final TimeBlockRepository timeBlockRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final TimeSlotCalculator timeSlotCalculator;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public List<AppointmentResponse> getAllAppointments() {
        log.info("Retrieving all appointments");
        return appointmentRepository.findAllByOrderByStartTimeDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LocalDateTime> getAvailableSlots(Long providerServiceOfferingId, LocalDate date) {
        ProviderServiceOffering offering = providerServiceOfferingRepository
                .findById(providerServiceOfferingId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        Provider provider = offering.getProvider();
        int duration = offering.getDurationOverride() != null ? offering.getDurationOverride() :
                offering.getServiceOffering().getDefaultDuration();
        int buffer = offering.getBufferMinutes() != null ? offering.getBufferMinutes() : 0;

        List<Availability> availabilities = availabilityRepository.findByProvider(provider);
        List<TimeBlock> timeBlocks = timeBlockRepository.findByProviderOverlappingDay(
                provider, date.atStartOfDay(), date.atTime(23, 59, 59));
        List<Appointment> existingAppointments = appointmentRepository.findByProviderOrderByStartTimeDesc(provider)
                .stream()
                .filter(a -> a.getStartTime().toLocalDate().equals(date))
                .toList();

        List<LocalDateTime> availableSlots = timeSlotCalculator.calculateAvailableSlots(date, availabilities, timeBlocks, existingAppointments,
                duration, buffer);
        log.info("Retrieved available slots for offering {} on {}", providerServiceOfferingId, date);
        return availableSlots;
    }

    @Transactional
    public void book(Long clientPersonId, BookAppointmentRequest request) {
        ProviderServiceOffering offering = providerServiceOfferingRepository
                .findById(request.getProviderServiceOfferingId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        Client client = clientRepository.findById(clientPersonId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        if (appointmentRepository.existsActiveBookingForClientAtTime(client, request.getStartTime())) {
            throw new IllegalStateException("You already have an appointment booked at this time.");
        }

        Provider provider = offering.getProvider();

        int duration = offering.getDurationOverride() != null ? offering.getDurationOverride() :
                offering.getServiceOffering().getDefaultDuration();

        LocalDateTime endTime = request.getStartTime().plusMinutes(duration);

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setProvider(provider);
        appointment.setProviderServiceOffering(offering);
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setClientNotes(request.getClientNotes());
        appointment.setDurationAtBooking(duration);
        appointment.setPriceAtBooking(offering.getEffectivePrice() != null ? offering.getEffectivePrice() :
                offering.getServiceOffering().getPriceEstimate());
        appointment.setServiceTitleSnapshot(offering.getServiceOffering().getTitle());
        appointmentRepository.save(appointment);

        User clientUser = userRepository.findById(clientPersonId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User providerUser = userRepository.findByPersonId(provider.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String timeDisplay = request.getStartTime().format(DISPLAY_FORMAT);
        String serviceName = offering.getServiceOffering().getTitle();
        String clientName = client.getFirstName() + " " + client.getLastName();
        String providerName = provider.getFirstName() + " " + provider.getLastName();

        notificationService.send(clientUser,
                "Your appointment request for " + serviceName + " on " + timeDisplay + " has been received",
                "/client/appointments");


        notificationService.send(providerUser,
                "New booking request from " + clientName + " for " + serviceName + " on " + timeDisplay,
                "/provider/appointments");

        emailService.sendAppointmentRequested(clientUser.getEmail(), clientName, providerName, serviceName,
                timeDisplay);
        emailService.sendNewBookingRequestToProvider(providerUser.getEmail(), providerName, clientName, serviceName,
                timeDisplay);
        log.info("Appointment booked: client={}, offering={}", clientPersonId, request.getProviderServiceOfferingId());
    }

    @Transactional
    public void confirm(Long appointmentId, Long providerPersonId) {
        Appointment appointment = getAndVerifyProvider(appointmentId, providerPersonId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmedAt(LocalDateTime.now());

        log.info("Appointment confirmed: id={}, provider={}", appointmentId, providerPersonId);

        User clientUser = userRepository.findById(appointment.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String timeDisplay = appointment.getStartTime().format(DISPLAY_FORMAT);
        String serviceName = appointment.getServiceTitleSnapshot();
        String clientName = appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName();
        String providerName = appointment.getProvider().getFirstName() + " " + appointment.getProvider().getLastName();

        notificationService.send(clientUser,
                "Your appointment for " + serviceName + " on " + timeDisplay + " has been confirmed",
                "/client/appointments");

        emailService.sendAppointmentConfirmed(clientUser.getEmail(), clientName, providerName, serviceName,
                timeDisplay);
    }

    @Transactional
    public void decline(Long appointmentId, Long providerPersonId) {
        Appointment appointment = getAndVerifyProvider(appointmentId, providerPersonId);
        appointment.setStatus(AppointmentStatus.DECLINED);
        appointment.setCancelledAt(LocalDateTime.now());

        User clientUser = userRepository.findById(appointment.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String timeDisplay = appointment.getStartTime().format(DISPLAY_FORMAT);
        String serviceName = appointment.getServiceTitleSnapshot();
        String clientName = appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName();
        String providerName = appointment.getProvider().getFirstName() + " " + appointment.getProvider().getLastName();

        notificationService.send(clientUser,
                "Your appointment request for " + serviceName + " on " + timeDisplay + " was declined",
                "/client/appointments");

        emailService.sendAppointmentDeclined(clientUser.getEmail(), clientName, providerName, serviceName, timeDisplay);
        log.info("Appointment declined: id={}, provider={}", appointmentId, providerPersonId);
    }

    public List<AppointmentResponse> getClientAppointments(Long clientPersonId) {
        log.debug("Retrieving appointments for client {}", clientPersonId);
        Client client = clientRepository.findById(clientPersonId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        return appointmentRepository.findByClientOrderByStartTimeDesc(client)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AppointmentResponse> getProviderAppointments(Long providerPersonId) {
        log.debug("Retrieving appointments for provider {}", providerPersonId);
        Provider provider = providerRepository.findById(providerPersonId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        return appointmentRepository.findByProviderOrderByStartTimeDesc(provider)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Appointment getAndVerifyProvider(Long appointmentId, Long providerPersonId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        if (!appointment.getProvider().getId().equals(providerPersonId)) {
            throw new SecurityException("Not authorized");
        }
        log.debug("Appointment found: id={}, provider={}", appointmentId, providerPersonId);
        return appointment;
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setClientFirstName(appointment.getClient().getFirstName());
        response.setClientLastName(appointment.getClient().getLastName());
        response.setProviderFirstName(appointment.getProvider().getFirstName());
        response.setProviderLastName(appointment.getProvider().getLastName());
        response.setServiceTitleSnapshot(appointment.getServiceTitleSnapshot());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setStatus(appointment.getStatus());
        response.setClientNotes(appointment.getClientNotes());
        response.setProviderNotes(appointment.getProviderNotes());
        response.setDurationAtBooking(appointment.getDurationAtBooking());
        response.setPriceAtBooking(appointment.getPriceAtBooking());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setConfirmedAt(appointment.getConfirmedAt());
        response.setCancelledAt(appointment.getCancelledAt());
        return response;
    }

    @Transactional
    public void markCompleted(Long appointmentId, Long providerPersonId) {
        Appointment appointment = getAndVerifyProvider(appointmentId, providerPersonId);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointments can be marked complete");
        }
        if (appointment.getEndTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot mark complete before appointment has ended");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedAt(LocalDateTime.now());
        log.info("Appointment marked completed: id={}", appointmentId);
    }

    @Transactional
    public void markNoShow(Long appointmentId, Long providerPersonId) {
        Appointment appointment = getAndVerifyProvider(appointmentId, providerPersonId);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointments can be marked as no-show");
        }
        if (appointment.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot mark no-show before appointment time");
        }
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        log.info("Appointment marked no-show: id={}", appointmentId);
    }

    @Transactional
    public void cancelByClient(Long appointmentId, Long clientPersonId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        if (!appointment.getClient().getId().equals(clientPersonId)) {
            throw new SecurityException("Not authorized");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }

        Integer hoursLimit = appointment.getProvider().getCancellationHoursLimit();
        if (hoursLimit != null && hoursLimit > 0) {
            LocalDateTime cutoff = appointment.getStartTime().minusHours(hoursLimit);
            if (LocalDateTime.now().isAfter(cutoff)) {
                throw new IllegalStateException("Cancellation is no longer possible because the appointment starts" +
                        " in less than " + hoursLimit + " hours. Please contact the provider directly.");
            }
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        log.info("Appointment cancelled by client: id={}, client={}", appointmentId, clientPersonId);

        User providerUser = userRepository.findByPersonId(appointment.getProvider().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.findById(clientPersonId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        String clientName = appointment.getClient().getFirstName() + " "
                + appointment.getClient().getLastName();
        String serviceName = appointment.getServiceTitleSnapshot();
        String timeDisplay = appointment.getStartTime().format(DISPLAY_FORMAT);

        notificationService.send(providerUser,
                clientName + " cancelled their appointment for "
                        + serviceName + " on " + timeDisplay,
                "/provider/appointments");

        emailService.sendAppointmentCancelledByClient(
                providerUser.getEmail(),
                providerUser.getPerson().getFirstName(),
                clientName, serviceName, timeDisplay);
    }

    @Transactional
    public void cancelByProvider(Long appointmentId, Long providerPersonId) {
        Appointment appointment = getAndVerifyProvider(appointmentId, providerPersonId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        User clientUser = userRepository.findById(appointment.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String providerName = appointment.getProvider().getFirstName() + " "
                + appointment.getProvider().getLastName();
        String serviceName = appointment.getServiceTitleSnapshot();
        String timeDisplay = appointment.getStartTime().format(DISPLAY_FORMAT);

        notificationService.send(clientUser,
                "Your appointment for " + serviceName + " on " + timeDisplay
                        + " was cancelled by the provider",
                "/client/appointments");

        emailService.sendAppointmentCancelledByProvider(
                clientUser.getEmail(),
                appointment.getClient().getFirstName(),
                providerName, serviceName, timeDisplay);
        log.info("Appointment cancelled by provider: id={}, provider={}", appointmentId, providerPersonId);
    }
}