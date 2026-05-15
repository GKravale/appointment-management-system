package com.appointment.system.controller;

import com.appointment.system.dto.request.UpdateProviderProfileRequest;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.AppointmentService;
import com.appointment.system.service.ProviderService;
import com.appointment.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final UserService userService;


    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("username", user.getUsername());
        List<AppointmentResponse> all = appointmentService.getProviderAppointments(user.getPersonId());
        long pending = all.stream().filter(a -> a.getStatus() == AppointmentStatus.REQUESTED).count();
        long confirmedToday =
                all.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED && a.getStartTime().toLocalDate()
                        .equals(java.time.LocalDate.now())).count();
        model.addAttribute("pendingCount", pending);
        model.addAttribute("todayCount", confirmedToday);
        model.addAttribute("recentAppointments", all.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED)
                .limit(3).toList());
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
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @ModelAttribute("updateRequest") UpdateProviderProfileRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", providerService.getProfile(user.getPersonId()));
            return "provider/profile";
        }

        providerService.updateProfile(user.getPersonId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        return "redirect:/provider/profile";
    }

    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal CustomUserDetails currentUser, HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        userService.deleteAccount(currentUser.getId());
        new org.springframework.security.web.authentication.logout
                .SecurityContextLogoutHandler()
                .logout(request, response, null);
        return "redirect:/auth/login?deleted=true";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal CustomUserDetails user,
                               Model model) {
        model.addAttribute("appointments",
                appointmentService.getProviderAppointments(user.getPersonId()));
        return "provider/appointments";
    }

    @PostMapping("/appointments/{id}/confirm")
    public String confirm(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails user,
                          RedirectAttributes redirectAttributes) {
        appointmentService.confirm(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment confirmed");
        return "redirect:/provider/appointments";
    }

    @PostMapping("/appointments/{id}/decline")
    public String decline(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails user,
                          RedirectAttributes redirectAttributes) {
        appointmentService.decline(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment declined");
        return "redirect:/provider/appointments";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails user,
                                    RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelByProvider(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/provider/appointments";
    }

    @PostMapping("/appointments/{id}/complete")
    public String markComplete(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes) {
        appointmentService.markCompleted(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment marked as completed");
        return "redirect:/provider/appointments";
    }

    @PostMapping("/appointments/{id}/noshow")
    public String markNoShow(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails user,
                             RedirectAttributes redirectAttributes) {
        appointmentService.markNoShow(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Marked as no show");
        return "redirect:/provider/appointments";
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
}