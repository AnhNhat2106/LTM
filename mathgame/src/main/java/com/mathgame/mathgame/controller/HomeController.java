package com.mathgame.mathgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/lobby";
    }

    @GetMapping("/lobby")
    public String lobby() {
        return "lobby";
    }

    @GetMapping("/practice-page")
    public String practicePage() {
        return "redirect:/practice";
    }

    @GetMapping("/practice-history-page")
    public String practiceHistoryPage() {
        return "redirect:/practice/history";
    }

    // ✅ FIX: truyền username vào view battle để JS đọc
    @GetMapping("/battle")
    public String battle(Model model, Principal principal) {
        model.addAttribute("me", principal != null ? principal.getName() : "");
        return "battle";
    }
}
