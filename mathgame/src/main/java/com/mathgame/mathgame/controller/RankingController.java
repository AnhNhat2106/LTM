package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.repository.BattleResultRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RankingController {

    private final BattleResultRepository battleResultRepository;

    public RankingController(BattleResultRepository battleResultRepository) {
        this.battleResultRepository = battleResultRepository;
    }

    @GetMapping("/ranking")
    public String ranking(Model model) {
        model.addAttribute("rows", battleResultRepository.getBattleRanking());
        return "ranking";
    }
}
