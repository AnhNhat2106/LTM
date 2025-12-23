package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.dto.BattleSummaryDto;
import com.mathgame.mathgame.repository.BattleResultRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final BattleResultRepository battleResultRepository;

    public HomeController(BattleResultRepository battleResultRepository) {
        this.battleResultRepository = battleResultRepository;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/lobby";
    }

    @GetMapping("/lobby")
    public String lobby(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "";
        BattleSummaryDto summary = username.isBlank() ? null : battleResultRepository.getSummary(username);
        long totalPoints = summary != null ? summary.getTotalPoints() : 0;
        long matches = summary != null ? summary.getMatches() : 0;
        long wins = summary != null ? summary.getWins() : 0;
        int winRate = matches > 0 ? (int) Math.round((wins * 100.0) / matches) : 0;

        model.addAttribute("username", username.isBlank() ? "Khách" : username);
        model.addAttribute("battlePoints", totalPoints);
        model.addAttribute("battleMatches", matches);
        model.addAttribute("battleWinRate", winRate);

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

    @GetMapping("/battle")
    public String battle(Model model, Principal principal) {
        model.addAttribute("me", principal != null ? principal.getName() : "");
        return "battle";
    }
}