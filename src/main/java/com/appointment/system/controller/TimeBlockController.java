package com.appointment.system.controller;

import com.appointment.system.enums.BlockType;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.TimeBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/provider/timeblocks")
@RequiredArgsConstructor
public class TimeBlockController {

    private final TimeBlockService timeBlockService;

    @PostMapping("/create")
    public String create(@AuthenticationPrincipal CustomUserDetails user, @RequestParam @DateTimeFormat(iso =
            DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime, @RequestParam @DateTimeFormat(iso =
            DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime, @RequestParam BlockType type,
                         @RequestParam(required = false) String note, RedirectAttributes redirectAttributes) {
        try {
            timeBlockService.create(user.getPersonId(), startDateTime, endDateTime, type, note);
            redirectAttributes.addFlashAttribute("successMessage", "Time block added");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/provider/availability";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                         RedirectAttributes redirectAttributes) {
        timeBlockService.delete(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Time block removed");
        return "redirect:/provider/availability";
    }
}