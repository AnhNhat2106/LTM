package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.repository.BattleResultRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BattleHistoryController {

    private final BattleResultRepository battleResultRepository;

    public BattleHistoryController(BattleResultRepository battleResultRepository) {
        this.battleResultRepository = battleResultRepository;
    }

    @GetMapping("/battle/history")
    public String history(Authentication auth, Model model) {
        String username = auth.getName();
        model.addAttribute("items", battleResultRepository.findByUsernameOrderByPlayedAtDesc(username));
        model.addAttribute("username", username);
        return "battle_history";
    }
}
