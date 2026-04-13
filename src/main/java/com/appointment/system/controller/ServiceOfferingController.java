package com.appointment.system.controller;

import com.appointment.system.dto.request.CreateServiceOfferingRequest;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.ServiceOfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/provider/services")
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    @GetMapping
    public String listServices(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("services",
                serviceOfferingService.getProviderOfferings(user.getPersonId()));
        model.addAttribute("createRequest", new CreateServiceOfferingRequest());
        model.addAttribute("categories",
                serviceOfferingService.getAllCategories());
        return "provider/services";
    }

    @PostMapping("/create")
    public String createService(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @ModelAttribute("createRequest") CreateServiceOfferingRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("services",
                    serviceOfferingService.getProviderOfferings(user.getPersonId()));
            model.addAttribute("categories",
                    serviceOfferingService.getAllCategories());
            return "provider/services";
        }

        serviceOfferingService.createAndAssign(user.getPersonId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Service added successfully");
        return "redirect:/provider/services";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateService(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        serviceOfferingService.deactivate(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Service removed");
        return "redirect:/provider/services";
    }
}