package com.appointment.system.controller;

import com.appointment.system.dto.request.BookAppointmentRequest;
import com.appointment.system.dto.request.UpdateClientProfileRequest;
import com.appointment.system.dto.response.ClientProfileResponse;
import com.appointment.system.dto.response.ProviderServiceOfferingResponse;
import com.appointment.system.entity.ProviderServiceOffering;
import com.appointment.system.entity.User;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.AppointmentService;
import com.appointment.system.service.ClientService;
import com.appointment.system.service.EmailService;
import com.appointment.system.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final AppointmentService appointmentService;

    private final ProviderServiceOfferingRepository providerServiceOfferingRepository;

    private final ClientService clientService;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("appointments", appointmentService.getClientAppointments(user.getPersonId()));
        return "client/dashboard";
    }

    @GetMapping("/book")
    public String bookingPage(@RequestParam Long providerId, @RequestParam Long serviceId,
                              @AuthenticationPrincipal CustomUserDetails user, Model model) {
        ProviderServiceOfferingResponse service =
                ProviderServiceOfferingResponse.from(providerServiceOfferingRepository.findById(serviceId)
                        .orElseThrow(() -> new EntityNotFoundException("Service not found")));

        model.addAttribute("service", service);
        model.addAttribute("providerId", providerId);
        model.addAttribute("bookRequest", new BookAppointmentRequest());
        return "client/book";
    }

    @GetMapping("/slots")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSlots(@RequestParam Long serviceId,
                                                              @RequestParam @DateTimeFormat(iso =
                                                                      DateTimeFormat.ISO.DATE) LocalDate date) {

        final String availableTitle = "Available";
        final String primaryColor = "#0d6efd";
        final long slotDurationMinutes = 30L;

        List<Map<String, Object>> events = appointmentService.getAvailableSlots(serviceId, date).stream()
                .map(slot -> {
                    String startTime = slot.toString();
                    String endTime = slot.plusMinutes(slotDurationMinutes).toString();

                    return Map.of(
                            "title", availableTitle,
                            "start", startTime,
                            "end", endTime,
                            "backgroundColor", primaryColor,
                            "borderColor", primaryColor,
                            "extendedProps", Map.of("startTime", startTime)
                    );
                })
                .toList();

        return ResponseEntity.ok(events);
    }

    @PostMapping("/book")
    public String confirmBooking(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute("bookRequest"
    ) BookAppointmentRequest request, RedirectAttributes redirectAttributes) {

        appointmentService.book(user.getPersonId(), request);
        redirectAttributes.addFlashAttribute("successMessage",
                "Appointment requested successfully! You will receive a confirmation email.");
        return "redirect:/client/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("appointments", appointmentService.getClientAppointments(user.getPersonId()));
        return "client/appointments";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        ClientProfileResponse profile = clientService.getProfile(user.getPersonId());

        UpdateClientProfileRequest updateRequest = new UpdateClientProfileRequest();
        updateRequest.setFirstName(profile.getFirstName());
        updateRequest.setLastName(profile.getLastName());
        updateRequest.setPhoneNr(profile.getPhoneNr());
        updateRequest.setNotes(profile.getNotes());

        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", updateRequest);
        return "client/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute(
                                        "updateRequest") UpdateClientProfileRequest request,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", clientService.getProfile(user.getPersonId()));
            return "client/profile";
        }

        clientService.updateProfile(user.getPersonId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        return "redirect:/client/profile";
    }

    @GetMapping("/request")
    public String requestPage(
            @RequestParam Long providerId,
            @RequestParam Long serviceId,
            Model model) {
        ProviderServiceOfferingResponse service =
                ProviderServiceOfferingResponse.from(
                        providerServiceOfferingRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found")));
        model.addAttribute("service", service);
        model.addAttribute("providerId", providerId);
        return "client/request";
    }

    @PostMapping("/request")
    public String submitRequest(@AuthenticationPrincipal CustomUserDetails currentUser, @RequestParam Long serviceId,
                                @RequestParam Long providerId, @RequestParam(required = false) String preferredDates,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String contactPreference,
                                RedirectAttributes redirectAttributes) {

        ProviderServiceOffering pso = providerServiceOfferingRepository
                .findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        User providerUser = userRepository.findByPersonId(providerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User clientUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String clientName = clientUser.getPerson().getFirstName() + " "
                + clientUser.getPerson().getLastName();
        String serviceName = pso.getServiceOffering().getTitle();

        notificationService.send(providerUser,
                "New consultation request from " + clientName
                        + " for " + serviceName,
                "/provider/appointments");

        emailService.sendConsultationRequest(
                providerUser.getEmail(),
                providerUser.getPerson().getFirstName(),
                clientName,
                serviceName,
                preferredDates,
                description,
                contactPreference,
                clientUser.getEmail());

        redirectAttributes.addFlashAttribute("successMessage",
                "Your consultation request has been sent. "
                        + "The provider will contact you directly.");
        return "redirect:/client/dashboard";
    }

    @GetMapping("/available-days")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> getAvailableDays(@RequestParam Long serviceId,
                                                                 @RequestParam @DateTimeFormat(iso =
                                                                         DateTimeFormat.ISO.DATE) LocalDate from,
                                                                 @RequestParam @DateTimeFormat(iso =
                                                                         DateTimeFormat.ISO.DATE) LocalDate to) {

        Map<String, Boolean> result = new java.util.LinkedHashMap<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            List<LocalDateTime> slots = appointmentService.getAvailableSlots(serviceId, current);
            result.put(current.toString(), !slots.isEmpty());
            current = current.plusDays(1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails user,
                                    RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelByClient(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/client/appointments";
    }
}