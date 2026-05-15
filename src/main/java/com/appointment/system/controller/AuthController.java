package com.appointment.system.controller;

import com.appointment.system.dto.request.ChangePasswordRequest;
import com.appointment.system.dto.request.ForgotPasswordRequest;
import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.dto.request.ResetPasswordRequest;
import com.appointment.system.enums.Role;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

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
            String msg = request.getRole() == Role.ROLE_PROVIDER
                    ? "Registration submitted. Your account is pending admin approval."
                    : "Registration successful, please log in";
            redirectAttributes.addFlashAttribute("successMessage", msg);
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", new Role[]{Role.ROLE_CLIENT, Role.ROLE_PROVIDER});
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotRequest") ForgotPasswordRequest request,
                                 BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        userService.initiatePasswordReset(request);
        redirectAttributes.addFlashAttribute("successMessage",
                "If an account with that email exists, a reset link has been sent.");
        return "redirect:/auth/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("resetRequest", new ResetPasswordRequest());
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetRequest") ResetPasswordRequest request,
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }
        try {
            userService.resetPassword(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset successfully. Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("changeRequest", new ChangePasswordRequest());
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails currentUser, @Valid @ModelAttribute(
            "changeRequest") ChangePasswordRequest request, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }
        try {
            userService.changePassword(currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
            String role = currentUser.getRole().name();
            if (role.equals("ROLE_PROVIDER")) {
                return "redirect:/provider/profile";
            } else if (role.equals("ROLE_CLIENT")) {
                return "redirect:/client/profile";
            }
            return "redirect:/";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/auth/change-password";
        }
    }
}