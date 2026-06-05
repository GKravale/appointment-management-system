package com.appointment.system.controller;

import com.appointment.system.dto.request.UpdateProviderProfileRequest;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.AppointmentService;
import com.appointment.system.service.PostService;
import com.appointment.system.service.ProviderService;
import com.appointment.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/provider")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {

    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final PostService postService;

    String appointmentsPage = "provider/appointments";

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("username", user.getUsername());
        List<AppointmentResponse> all = appointmentService.getProviderAppointments(user.getPersonId());
        long pending = all.stream().filter(appointmentResponse ->
                appointmentResponse.getStatus() == AppointmentStatus.REQUESTED).count();
        long confirmedToday = all.stream().filter(appointmentResponse ->
                appointmentResponse.getStatus() == AppointmentStatus.CONFIRMED && appointmentResponse.getStartTime()
                        .toLocalDate().equals(java.time.LocalDate.now())).count();
        model.addAttribute("pendingCount", pending);
        model.addAttribute("todayCount", confirmedToday);
        model.addAttribute("recentAppointments", all.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED)
                .limit(3).toList());
        model.addAttribute("recentPosts", postService.getProviderPosts(user.getPersonId())
                .stream().limit(3).toList());
        return "provider/dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        ProviderProfileResponse profile = providerService.getProfile(user.getPersonId());

        UpdateProviderProfileRequest updateRequest = new UpdateProviderProfileRequest();
        updateRequest.setFirstName(profile.getFirstName());
        updateRequest.setLastName(profile.getLastName());
        updateRequest.setPhoneNr(profile.getPhoneNr());
        updateRequest.setLocation(profile.getLocation());
        updateRequest.setBio(profile.getBio());
        updateRequest.setCancellationHoursLimit(profile.getCancellationHoursLimit());

        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", updateRequest);
        return "provider/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute(
                                        "updateRequest") UpdateProviderProfileRequest request,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", providerService.getProfile(user.getPersonId()));
            return "provider/profile";
        }
        providerService.updateProfile(user.getPersonId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        log.info("Provider {} updated their profile", user.getUsername());
        return "redirect:/provider/profile";
    }

    @PostMapping("/profile/picture")
    public String uploadProfilePicture(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestParam("file") MultipartFile file,
                                       RedirectAttributes redirectAttributes) {
        try {
            providerService.uploadProfilePicture(user.getPersonId(), file);
            redirectAttributes.addFlashAttribute("successMessage", "Profile picture updated");
        } catch (Exception e) {
            log.error("Profile picture upload failed for user {}: {}", user.getPersonId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Provider {} updated their profile picture", user.getUsername());
        return "redirect:/provider/profile";
    }

    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal CustomUserDetails currentUser, HttpServletRequest request,
                                HttpServletResponse response) {
        userService.deleteAccount(currentUser.getId());
        log.info("Provider {} deleted account", currentUser.getUsername());
        new org.springframework.security.web.authentication.logout
                .SecurityContextLogoutHandler()
                .logout(request, response, null);
        return "redirect:/auth/login?deleted=true";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("appointments", appointmentService.getProviderAppointments(user.getPersonId()));
        return appointmentsPage;
    }

    @PostMapping("/appointments/{id}/confirm")
    public String confirm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                          RedirectAttributes redirectAttributes) {
        try {
            appointmentService.confirm(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment confirmed");
            log.info("Provider {} confirmed appointment {}", user.getUsername(), id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.warn("Provider {} failed to confirm appointment {}: {}", user.getUsername(), id, e.getMessage());
        }
        return appointmentsPage;
    }

    @PostMapping("/appointments/{id}/decline")
    public String decline(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                          RedirectAttributes redirectAttributes) {
        try {
            appointmentService.decline(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment declined");
            log.info("Provider {} declined appointment {}", user.getUsername(), id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.warn("Provider {} failed to decline appointment {}: {}", user.getUsername(), id, e.getMessage());
        }
        return appointmentsPage;
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                                    RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelByProvider(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Provider {} cancelled appointment {}", user.getUsername(), id);
        return appointmentsPage;
    }

    @PostMapping("/appointments/{id}/complete")
    public String markComplete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes) {
        try {
            appointmentService.markCompleted(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment marked as completed");
            log.info("Provider {} marked appointment {} as completed", user.getUsername(), id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.warn("Provider {} failed to mark appointment as completed {}: {}", user.getUsername(), id,
                    e.getMessage());
        }
        return appointmentsPage;
    }

    @PostMapping("/appointments/{id}/noshow")
    public String markNoShow(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                             RedirectAttributes redirectAttributes) {
        try {
            appointmentService.markNoShow(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Marked as no show");
            log.info("Provider {} marked appointment {} as no show", user.getUsername(), id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.warn("Provider {} failed to mark appointment as no show {}: {}", user.getUsername(), id,
                    e.getMessage());
        }
        return appointmentsPage;
    }

    @GetMapping("/schedule")
    public String schedule(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("appointments", appointmentService.getProviderAppointments(user.getPersonId()));
        return "provider/schedule";
    }

    @GetMapping("/schedule/events")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> scheduleEvents(@AuthenticationPrincipal CustomUserDetails user) {
        List<Map<String, Object>> events = appointmentService
                .getProviderAppointments(user.getPersonId())
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(a -> {
                    String color = switch (a.getStatus()) {
                        case REQUESTED -> "#ffc107";
                        case CONFIRMED -> "#0d6efd";
                        case COMPLETED -> "#198754";
                        default -> "#6c757d";
                    };
                    return Map.<String, Object>of(
                            "id", a.getId(),
                            "title", a.getClientFirstName() + " " + a.getClientLastName()
                                    + " — " + a.getServiceTitleSnapshot(),
                            "start", a.getStartTime().toString(),
                            "end", a.getEndTime().toString(),
                            "backgroundColor", color,
                            "borderColor", color,
                            "extendedProps", Map.of(
                                    "status", a.getStatus().toString(),
                                    "appointmentId", a.getId()
                            )
                    );
                })
                .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/test-principal")
    @ResponseBody
    public String testPrincipal(
            @AuthenticationPrincipal Object principal) {
        return principal == null ? "null" : principal.getClass().getName();
    }
}