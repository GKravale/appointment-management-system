package com.appointment.system.controller;

import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.enums.Role;
import com.appointment.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("roles", new Role[]{Role.ROLE_CLIENT, Role.ROLE_PROVIDER});
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userService.usernameExists(request.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "Username already taken");
        }
        if (userService.emailExists(request.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email already registered");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.ROLE_CLIENT, Role.ROLE_PROVIDER});
            return "auth/register";
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful, please log in");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", new Role[]{Role.ROLE_CLIENT, Role.ROLE_PROVIDER});
            return "auth/register";
        }
    }
}