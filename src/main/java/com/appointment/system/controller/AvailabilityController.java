package com.appointment.system.controller;

import com.appointment.system.dto.request.CreateAvailabilityRequest;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/provider/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public String availabilityPage(
            @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("availabilities",
                availabilityService.getAvailability(user.getPersonId()));
        model.addAttribute("createRequest", new CreateAvailabilityRequest());
        model.addAttribute("days", availabilityService.getAllDays());
        return "provider/availability";
    }

    @PostMapping("/create")
    public String create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @ModelAttribute("createRequest") CreateAvailabilityRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("availabilities",
                    availabilityService.getAvailability(user.getPersonId()));
            model.addAttribute("days", availabilityService.getAllDays());
            return "provider/availability";
        }

        try {
            availabilityService.create(user.getPersonId(), request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Availability added");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/provider/availability";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {
        availabilityService.delete(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Availability removed");
        return "redirect:/provider/availability";
    }
}