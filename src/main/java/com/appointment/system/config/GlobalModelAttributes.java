package com.appointment.system.config;

import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @ModelAttribute
    public void addNotificationCount(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser != null) {
            userRepository.findById(currentUser.getId()).ifPresent(user -> model.addAttribute(
                    "unreadNotificationCount", notificationService.countUnread(user)));
        }
    }
}