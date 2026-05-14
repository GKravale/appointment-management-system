package com.appointment.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendAppointmentRequested(String toEmail, String clientName, String providerName, String serviceName, String startTime) {
        send(toEmail,
                "Appointment request received",
                "Hi " + clientName + ",\n\n" +
                        "Your appointment request has been received.\n\n" +
                        "Provider: " + providerName + "\n" +
                        "Service: " + serviceName + "\n" +
                        "Requested time: " + startTime + "\n\n" +
                        "You will be notified once the provider confirms or declines your request.\n\n" +
                        "Thank you for using our system.");
    }

    @Async
    public void sendAppointmentConfirmed(String toEmail, String clientName, String providerName, String serviceName, String startTime) {
        send(toEmail,
                "Appointment confirmed",
                "Hi " + clientName + ",\n\n" +
                        "Great news! Your appointment has been confirmed.\n\n" +
                        "Provider: " + providerName + "\n" +
                        "Service: " + serviceName + "\n" +
                        "Time: " + startTime + "\n\n" +
                        "See you then!");
    }

    @Async
    public void sendAppointmentDeclined(String toEmail, String clientName, String providerName, String serviceName, String startTime) {
        send(toEmail,
                "Appointment request declined",
                "Hi " + clientName + ",\n\n" +
                        "Unfortunately your appointment request was declined.\n\n" +
                        "Provider: " + providerName + "\n" +
                        "Service: " + serviceName + "\n" +
                        "Requested time: " + startTime + "\n\n" +
                        "Please visit our platform to find another available time or provider.");
    }

    @Async
    public void sendAppointmentCancelledByClient(String toEmail, String providerName,
                                                 String clientName, String serviceName,
                                                 String startTime) {
        send(toEmail,
                "Appointment cancelled — " + serviceName,
                "Hi " + providerName + ",\n\n" +
                        clientName + " has cancelled their appointment.\n\n" +
                        "Service: " + serviceName + "\n" +
                        "Time: " + startTime + "\n\n" +
                        "The slot is now available for new bookings.");
    }

    @Async
    public void sendAppointmentCancelledByProvider(String toEmail, String clientName,
                                                   String providerName, String serviceName,
                                                   String startTime) {
        send(toEmail,
                "Appointment cancelled — " + serviceName,
                "Hi " + clientName + ",\n\n" +
                        "Your appointment has been cancelled by the provider.\n\n" +
                        "Provider: " + providerName + "\n" +
                        "Service: " + serviceName + "\n" +
                        "Time: " + startTime + "\n\n" +
                        "Please visit our platform to book a new appointment.");
    }

    @Async
    public void sendNewBookingRequestToProvider(String toEmail, String providerName, String clientName, String serviceName, String startTime) {
        send(toEmail,
                "New appointment request",
                "Hi " + providerName + ",\n\n" +
                        "You have a new appointment request.\n\n" +
                        "Client: " + clientName + "\n" +
                        "Service: " + serviceName + "\n" +
                        "Requested time: " + startTime + "\n\n" +
                        "Please log in to confirm or decline.");
    }

    @Async
    public void sendConsultationRequest(String toEmail, String providerName, String clientName, String serviceName, String preferredDates, String description, String contactPreference, String clientEmail) {
        send(toEmail,
                "New consultation request — " + serviceName,
                "Hi " + providerName + ",\n\n" +
                        "You have a new consultation request.\n\n" +
                        "Client: " + clientName + "\n" +
                        "Service: " + serviceName + "\n" +
                        "Preferred dates: " + (preferredDates != null ? preferredDates : "Not specified") + "\n" +
                        "Description: " + (description != null ? description : "Not specified") + "\n" +
                        "Contact preference: " + (contactPreference != null ? contactPreference : "Not specified") + "\n" +
                        "Client email: " + clientEmail + "\n\n" +
                        "Please contact the client directly to arrange the appointment.");
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        String resetLink = baseUrl + "/auth/reset-password?token=" + token;
        send(toEmail,
                "Password reset request",
                "Hi " + username + ",\n\n" +
                        "You requested a password reset for your account.\n\n" +
                        "Click the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 1 hour.\n\n" +
                        "If you did not request this, please ignore this email or contact support. " +
                        "Your password will not be changed.\n\n" +
                        "Appointment system.");
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}