package com.appointment.system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("Should send appointment requested email with correct subject and recipient")
    void sendAppointmentRequested_sendsEmail() {
        emailService.sendAppointmentRequested("anna@test.com", "Anna", "Līga", "Haircut", "Monday 10:00");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("anna@test.com", msg.getTo()[0]);
        assertEquals("Appointment request received", msg.getSubject());
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("Anna"));
        assertTrue(msg.getText().contains("Līga"));
        assertTrue(msg.getText().contains("Haircut"));
    }

    @Test
    @DisplayName("Should send appointment confirmed email")
    void sendAppointmentConfirmed_sendsEmail() {
        emailService.sendAppointmentConfirmed("anna@test.com", "Anna", "Līga", "Haircut", "Monday 10:00");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("anna@test.com", msg.getTo()[0]);
        assertEquals("Appointment confirmed", msg.getSubject());
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("confirmed"));
    }

    @Test
    @DisplayName("Should send appointment declined email")
    void sendAppointmentDeclined_sendsEmail() {
        emailService.sendAppointmentDeclined("anna@test.com", "Anna", "Līga", "Haircut", "Monday 10:00");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertEquals("Appointment request declined", msg.getSubject());
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("declined"));
    }


    @Test
    @DisplayName("Should send cancellation-by-client email to provider")
    void sendAppointmentCancelledByClient_sendsEmailToProvider() {
        emailService.sendAppointmentCancelledByClient("liga@test.com", "Līga", "Anna", "Haircut", "Monday 10:00");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("liga@test.com", msg.getTo()[0]);
        assert msg.getSubject() != null;
        assertTrue(msg.getSubject().contains("cancelled"));
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("Anna"));
    }

    @Test
    @DisplayName("Should send cancellation-by-provider email to client")
    void sendAppointmentCancelledByProvider_sendsEmailToClient() {
        emailService.sendAppointmentCancelledByProvider("anna@test.com", "Anna", "Līga", "Haircut", "Monday 10:00");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("anna@test.com", msg.getTo()[0]);
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("cancelled by the provider"));
    }

    @Test
    @DisplayName("Should send new booking request email to provider")
    void sendNewBookingRequestToProvider_sendsEmail() {
        emailService.sendNewBookingRequestToProvider("liga@test.com", "Līga", "Anna", "Haircut", "Monday 10:00");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("liga@test.com", msg.getTo()[0]);
        assertEquals("New appointment request", msg.getSubject());
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("Anna"));
    }

    @Test
    @DisplayName("Should send password reset email with reset link containing token")
    void sendPasswordResetEmail_containsTokenLink() {
        emailService.sendPasswordResetEmail("anna@test.com", "anna123", "abc-reset-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("anna@test.com", msg.getTo()[0]);
        assertEquals("Password reset request", msg.getSubject());
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("abc-reset-token"));
        assertTrue(msg.getText().contains("http://localhost:8080/auth/reset-password?token=abc-reset-token"));
    }

    @Test
    @DisplayName("Should send consultation request email to provider")
    void sendConsultationRequest_sendsEmail() {
        emailService.sendConsultationRequest("liga@test.com", "Līga", "Anna", "Tattoo", "Next week", "Custom design",
                "Email", "anna@test.com");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assertEquals("liga@test.com", msg.getTo()[0]);
        assert msg.getSubject() != null;
        assertTrue(msg.getSubject().contains("Tattoo"));
        assert msg.getText() != null;
        assertTrue(msg.getText().contains("Anna"));
        assertTrue(msg.getText().contains("Custom design"));
    }

    @Test
    @DisplayName("Should always set correct from address on all emails")
    void allEmails_useConfiguredFromAddress() {
        emailService.sendAppointmentConfirmed("anna@test.com", "Anna", "Līga", "Haircut", "Monday");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("noreply@test.com", captor.getValue().getFrom());
    }
}