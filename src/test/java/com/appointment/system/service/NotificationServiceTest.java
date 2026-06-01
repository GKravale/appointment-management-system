package com.appointment.system.service;

import com.appointment.system.entity.Notification;
import com.appointment.system.entity.User;
import com.appointment.system.repository.NotificationRepository;
import com.appointment.system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private NotificationService notificationService;

    private User user() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    @Test
    @DisplayName("Should save a notification when send is called")
    void send_savesNotification() {
        User user = user();
        notificationService.send(user, "Appointment confirmed", "/client/appointments");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should return notifications for user in descending order")
    void getForUser_returnsNotifications() {
        User user = user();
        Notification notification1 = new Notification();
        Notification notification2 = new Notification();

        when(notificationRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification1, notification2));

        List<Notification> result = notificationService.getForUser(user);

        assertEquals(2, result.size());
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    @DisplayName("Should return correct unread count")
    void countUnread_returnsCorrectCount() {
        User user = user();
        when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(3L);

        long count = notificationService.countUnread(user);

        assertEquals(3L, count);
    }

    @Test
    @DisplayName("Should mark all notifications as read for a user")
    void markAllRead_marksAllNotificationsRead() {
        User user = user();
        Notification notification1 = new Notification();
        notification1.setIsRead(false);
        Notification notification2 = new Notification();
        notification2.setIsRead(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification1, notification2));

        notificationService.markAllRead(1L);

        assertTrue(notification1.getIsRead());
        assertTrue(notification2.getIsRead());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found on markAllRead")
    void markAllRead_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> notificationService.markAllRead(99L));
    }

    @Test
    @DisplayName("Should mark a single notification as read")
    void markRead_setsIsReadTrue() {
        Notification notification = new Notification();
        notification.setIsRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markRead(1L);

        assertTrue(notification.getIsRead());
    }

    @Test
    @DisplayName("Should do nothing silently when notification not found on markRead")
    void markRead_notFound_doesNothing() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> notificationService.markRead(99L));
    }
}