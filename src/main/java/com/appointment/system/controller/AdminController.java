package com.appointment.system.controller;

import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.repository.AppointmentRepository;
import com.appointment.system.repository.ClientRepository;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.service.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalProviders", providerRepository.count());
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("totalAppointments", appointmentRepository.count());
        model.addAttribute("pendingProviders",
                userRepository.findByRoleAndAccountStatus(
                        com.appointment.system.enums.Role.ROLE_PROVIDER,
                        AccountStatus.PENDING));
        return "admin/dashboard";
    }

    @GetMapping("/providers")
    public String providers(Model model) {
        model.addAttribute("providers",
                userRepository.findByRole(
                        com.appointment.system.enums.Role.ROLE_PROVIDER));
        return "admin/providers";
    }

    @PostMapping("/providers/{id}/approve")
    public String approveProvider(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Provider approved");
        return "redirect:/admin/providers";
    }

    @PostMapping("/providers/{id}/suspend")
    public String suspendProvider(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Provider suspended");
        return "redirect:/admin/providers";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "User deactivated");
        return "redirect:/admin/users";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments",
                appointmentService.getAllAppointments());
        return "admin/appointments";
    }
}