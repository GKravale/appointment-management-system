package com.appointment.system.controller;

import com.appointment.system.entity.Post;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.service.PortfolioService;
import com.appointment.system.service.PostService;
import com.appointment.system.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/providers")
@RequiredArgsConstructor
public class ProvidersController {

    private final ProviderService providerService;
    private final PortfolioService portfolioService;
    private final PostService postService;

    @GetMapping
    public String browseProviders(@RequestParam(required = false) ServiceCategory category, Model model) {
        if (category != null) {
            model.addAttribute("providers", providerService.getActiveProvidersByCategory(category));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("providers", providerService.getAllActiveProviders());
        }
        model.addAttribute("categories", ServiceCategory.values());
        return "providers/browse";
    }

    @GetMapping("/{id}")
    public String providerProfile(@PathVariable Long id, Model model) {
        model.addAttribute("provider", providerService.getPublicProfile(id));
        model.addAttribute("assets", portfolioService.getPortfolioAssets(id));
        List<Post> allPosts = postService.getProviderPosts(id);
        model.addAttribute("posts", allPosts);
        model.addAttribute("totalPosts", allPosts.size());
        return "providers/profile";
    }
}