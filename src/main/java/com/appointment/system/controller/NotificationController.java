package com.appointment.system.controller;

import com.appointment.system.entity.User;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public String notificationsPage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new EntityNotFoundException("User " +
                "not found"));
        notificationService.markAllRead(user.getId());
        model.addAttribute("notifications", notificationService.getForUser(user));
        return "notifications";
    }

    @PostMapping("/read/{id}")
    @ResponseBody
    public void markRead(@PathVariable Long id) {
        notificationService.markRead(id);
    }
}