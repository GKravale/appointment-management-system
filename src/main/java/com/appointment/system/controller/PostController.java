package com.appointment.system.controller;

import com.appointment.system.service.PortfolioService;
import com.appointment.system.service.PostService;
import com.appointment.system.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/provider/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PortfolioService portfolioService;

    @GetMapping
    public String postsPage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("posts", postService.getProviderPosts(user.getPersonId()));
        model.addAttribute("portfolioAssets", portfolioService.getPortfolioAssets(user.getPersonId()));
        return "provider/posts";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.getById(id));
        return "provider/post-detail";
    }

    @PostMapping("/create")
    public String createPost(@AuthenticationPrincipal CustomUserDetails user, @RequestParam(value = "contentText",
                                     required = false) String contentText,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
                             @RequestParam(value = "portfolioAssetIds", required = false) List<Long> portfolioAssetIds,
                             @RequestParam(value = "addToPortfolio", required = false, defaultValue = "false")
                             boolean addToPortfolio, RedirectAttributes redirectAttributes) {
        try {
            postService.createPost(user.getPersonId(), contentText, files, portfolioAssetIds, addToPortfolio);
            redirectAttributes.addFlashAttribute("successMessage", "Post published");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Provider {} published a post", user.getUsername());
        return "redirect:/provider/posts";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                             RedirectAttributes redirectAttributes) {
        postService.deletePost(id, user.getPersonId());
        redirectAttributes.addFlashAttribute("successMessage", "Post deleted");
        log.info("Provider {} deleted a post", user.getUsername());
        return "redirect:/provider/posts";
    }
}