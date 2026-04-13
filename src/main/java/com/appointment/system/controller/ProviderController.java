package com.appointment.system.controller;

import com.appointment.system.dto.request.UpdateProviderProfileRequest;
import com.appointment.system.dto.response.ProviderProfileResponse;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("username", user.getUsername());
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
}