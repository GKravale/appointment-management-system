package com.appointment.system.controller;

import com.appointment.system.dto.request.CreateAvailabilityRequest;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.AvailabilityService;
import com.appointment.system.service.TimeBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/provider/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    private final TimeBlockService timeBlockService;

    @GetMapping
    public String availabilityPage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("availabilities",
                availabilityService.getAvailability(user.getPersonId()));
        model.addAttribute("createRequest", new CreateAvailabilityRequest());
        model.addAttribute("days", availabilityService.getAllDays());
        model.addAttribute("timeBlocks", timeBlockService.getTimeBlocks(user.getPersonId()));
        model.addAttribute("blockTypes", timeBlockService.getAllBlockTypes());
        return "provider/availability";
    }

    @PostMapping("/create")
    public String create(@AuthenticationPrincipal CustomUserDetails user,
                         @Valid @ModelAttribute("createRequest") CreateAvailabilityRequest request,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("availabilities", availabilityService.getAvailability(user.getPersonId()));
            model.addAttribute("days", availabilityService.getAllDays());
            model.addAttribute("timeBlocks", timeBlockService.getTimeBlocks(user.getPersonId()));
            model.addAttribute("blockTypes", timeBlockService.getAllBlockTypes());
            return "provider/availability";
        }
        try {
            availabilityService.create(user.getPersonId(), request);
            log.info("Provider {} created availability on {} from {} to {}", user.getPersonId(), request.getDayOfWeek(),
                    request.getStartTime(), request.getEndTime());
            redirectAttributes.addFlashAttribute("successMessage", "Availability added");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/provider/availability";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                         RedirectAttributes redirectAttributes) {
        availabilityService.delete(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Availability removed");
        log.info("Provider {} deleted availability {}", user.getPersonId(), id);
        return "redirect:/provider/availability";
    }
}