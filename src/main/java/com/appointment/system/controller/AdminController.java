package com.appointment.system.controller;

import com.appointment.system.dto.request.BookAppointmentRequest;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.AppointmentStatus;
import com.appointment.system.enums.Role;
import com.appointment.system.repository.*;
import com.appointment.system.service.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProviderServiceOfferingRepository providerServiceOfferingRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalProviders", providerRepository.count());
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("totalAppointments", appointmentRepository.count());
        model.addAttribute("pendingProviders",
                userRepository.findByRoleAndAccountStatus(com.appointment.system.enums.Role.ROLE_PROVIDER,
                        AccountStatus.PENDING));
        return "admin/dashboard";
    }

    @GetMapping("/providers")
    public String providers(Model model) {
        model.addAttribute("providers",
                userRepository.findByRole(com.appointment.system.enums.Role.ROLE_PROVIDER));
        return "admin/providers";
    }

    @PostMapping("/providers/{id}/approve")
    public String approveProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
            user.setAccountStatus(AccountStatus.ACTIVE);
            if (user.getPerson() instanceof Provider provider) {
                provider.setIsActive(true);
                provider.setIsDeleted(false);
                providerRepository.save(provider);
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Provider approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/providers";
    }

    @PostMapping("/providers/{id}/suspend")
    public String suspendProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Provider suspended");
        log.info("Provider {} suspended by admin", id);
        return "redirect:/admin/providers";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
            user.setAccountStatus(AccountStatus.SUSPENDED);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "User deactivated");
            log.info("User {} deactivated by admin", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/appointments")
    public String appointments(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String providerName, Model model) {
        List<AppointmentResponse> all = appointmentService.getAllAppointments();

        if (status != null && !status.isEmpty()) {
            all = all.stream().filter(a -> a.getStatus().name().equals(status)).toList();
        }
        if (providerName != null && !providerName.isEmpty()) {
            String search = providerName.toLowerCase();
            all = all.stream().filter(a -> (a.getProviderFirstName() + " " + a.getProviderLastName())
                    .toLowerCase().contains(search)).toList();
        }

        model.addAttribute("appointments", all);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProvider", providerName);
        return "admin/appointments";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clients", userRepository.findByRole(Role.ROLE_CLIENT));
        return "admin/clients";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("services", serviceOfferingRepository.findAll());
        return "admin/services";
    }

    @PostMapping("/services/{id}/deactivate")
    public String deactivateService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        serviceOfferingRepository.findById(id).ifPresent(serviceOffering -> {
            serviceOffering.setIsActive(false);
            serviceOfferingRepository.save(serviceOffering);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Service deactivated");
        log.info("Service {} deactivated by admin", id);
        return "redirect:/admin/services";
    }

    @PostMapping("/services/{id}/activate")
    public String activateService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        serviceOfferingRepository.findById(id).ifPresent(serviceOffering -> {
            serviceOffering.setIsActive(true);
            serviceOfferingRepository.save(serviceOffering);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Service activated");
        log.info("Service {} activated by admin", id);
        return "redirect:/admin/services";
    }

    @PostMapping("/users/{id}/reactivate")
    public String reactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "User reactivated");
        log.info("User {} reactivated by admin", user.getUsername());
        return "redirect:/admin/users";
    }

    @PostMapping("/providers/{id}/reactivate")
    public String reactivateProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        if (user.getPerson() instanceof Provider provider) {
            provider.setIsActive(true);
            provider.setIsDeleted(false);
            providerRepository.save(provider);
        }
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Provider reactivated");
        log.info("Provider {} reactivated by admin", user.getUsername());
        return "redirect:/admin/providers";
    }

    @PostMapping("/providers/{id}/delete")
    public String deleteProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.getPerson().setIsDeleted(true);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Provider deleted");
        log.info("Provider {} deleted by admin", user.getUsername());
        return "redirect:/admin/providers";
    }

    @PostMapping("/clients/{id}/reactivate")
    public String reactivateClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Client reactivated");
        log.info("Client {} reactivated by admin", user.getUsername());
        return "redirect:/admin/clients";
    }

    @PostMapping("/clients/{id}/delete")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.getPerson().setIsDeleted(true);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Client deleted");
        log.info("Client {} deleted by admin", user.getUsername());
        return "redirect:/admin/clients";
    }

    @PostMapping("/clients/{id}/deactivate")
    public String deactivateClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Client deactivated");
        log.info("Client {} deactivated by admin", user.getUsername());
        return "redirect:/admin/clients";
    }

    @GetMapping("/schedule")
    public String schedulePicker(Model model) {
        model.addAttribute("providers", providerRepository.findByIsActiveTrueAndIsDeletedFalse());
        return "admin/schedule-picker";
    }

    @GetMapping("/schedule/{providerId}")
    public String scheduleView(@PathVariable Long providerId, Model model) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        model.addAttribute("provider", provider);
        model.addAttribute("services",
                providerServiceOfferingRepository.findByProviderAndIsActiveTrueAndIsDeletedFalse(provider));
        model.addAttribute("clients", userRepository.findByRole(Role.ROLE_CLIENT));
        return "admin/schedule";
    }

    @GetMapping("/schedule/{providerId}/events")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> scheduleEvents(@PathVariable Long providerId) {
        List<Map<String, Object>> events = appointmentService
                .getProviderAppointments(providerId)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(a -> {
                    String color = switch (a.getStatus()) {
                        case REQUESTED -> "#ffc107";
                        case CONFIRMED -> "#0d6efd";
                        case COMPLETED -> "#198754";
                        default -> "#6c757d";
                    };
                    return Map.of(
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

    @GetMapping("/schedule/{providerId}/slots")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> scheduleSlots(@PathVariable Long providerId,
                                                                   @RequestParam Long serviceId,
                                                                   @RequestParam @DateTimeFormat(iso =
                                                                           DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Map<String, Object>> events = appointmentService.getAvailableSlots(serviceId, date)
                .stream()
                .map(slot -> Map.of(
                        "title", "Available",
                        "start", slot.toString(),
                        "end", slot.plusMinutes(30).toString(),
                        "backgroundColor", "#198754",
                        "borderColor", "#198754",
                        "extendedProps", Map.of("startTime", slot.toString())
                ))
                .toList();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/schedule/{providerId}/book")
    public String adminBook(@PathVariable Long providerId, @RequestParam Long clientUserId,
                            @RequestParam Long providerServiceOfferingId, @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                            @RequestParam(required = false) String clientNotes, RedirectAttributes redirectAttributes) {
        try {
            Long clientPersonId = userRepository.findById(clientUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"))
                    .getPerson().getId();
            BookAppointmentRequest request = new BookAppointmentRequest();
            request.setProviderServiceOfferingId(providerServiceOfferingId);
            request.setStartTime(startTime);
            request.setClientNotes(clientNotes);
            appointmentService.book(clientPersonId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment booked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Admin booked appointment for client {} with provider {}", clientUserId, providerId);
        return "redirect:/admin/schedule/" + providerId;
    }
}