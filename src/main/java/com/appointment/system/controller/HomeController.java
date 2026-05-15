package com.appointment.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
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