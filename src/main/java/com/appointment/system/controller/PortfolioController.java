package com.appointment.system.controller;

import com.appointment.system.service.PortfolioService;
import com.appointment.system.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/provider/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public String portfolioPage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("assets", portfolioService.getPortfolioAssets(user.getPersonId()));
        return "provider/portfolio";
    }

    @PostMapping("/upload")
    public String uploadMedia(@AuthenticationPrincipal CustomUserDetails user,
                              @RequestParam("file") MultipartFile file, @RequestParam(value = "altText", required =
                    false) String altText, RedirectAttributes redirectAttributes) {
        try {
            portfolioService.uploadMedia(user.getPersonId(), file, altText);
            redirectAttributes.addFlashAttribute("successMessage", "Image uploaded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Provider {} uploaded image {} in their portfolio", user.getUsername(), file.getOriginalFilename());
        return "redirect:/provider/portfolio";
    }

    @PostMapping("/delete/{id}")
    public String deleteMedia(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        try {
            portfolioService.deleteMedia(id, user.getPersonId());
            redirectAttributes.addFlashAttribute("successMessage", "Image deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Provider {} deleted image {} in their portfolio", user.getUsername(), id);
        return "redirect:/provider/portfolio";
    }
}