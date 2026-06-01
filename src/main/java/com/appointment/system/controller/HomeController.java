package com.appointment.system.controller;

import com.appointment.system.enums.ServiceCategory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categoryIcons", java.util.Arrays.asList(ServiceCategory.values()));
        return "home";
    }

    @GetMapping("/privacy")
    public String privacyPolicy() {
        return "legal/privacy";
    }

    @GetMapping("/terms")
    public String termsOfService() {
        return "legal/terms";
    }
}