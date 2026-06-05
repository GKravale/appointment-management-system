package com.appointment.system.service;

import com.appointment.system.entity.Notification;
import com.appointment.system.entity.User;
import com.appointment.system.repository.NotificationRepository;
import com.appointment.system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    public void send(User user, String message, String link) {
        notificationRepository.save(new Notification(user, message, link));
        log.debug("Notification sent to user {}: {}", user.getId(), message);
    }

    public List<Notification> getForUser(User user) {
        log.debug("Retrieving notifications for user {}", user.getId());
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAllRead(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        notificationRepository.findByUserOrderByCreatedAtDesc(user).forEach(n -> n.setIsRead(true));
        log.info("All notifications marked as read for user {}", userId);
    }

    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> n.setIsRead(true));
        log.debug("Notification {} marked as read", notificationId);
    }
}