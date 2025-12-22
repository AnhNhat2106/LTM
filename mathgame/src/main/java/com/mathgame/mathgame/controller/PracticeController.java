package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.dto.QuestionDto;
import com.mathgame.mathgame.entity.PracticeHistory;
import com.mathgame.mathgame.service.PracticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    // ================== START PRACTICE ==================
    @GetMapping("/practice")
    public String practice(Model model, HttpSession session) {

        int total = 5; // MVP: 5 câu

        session.setAttribute("p_total", total);
        session.setAttribute("p_index", 0);
        session.setAttribute("p_correct", 0);
        session.setAttribute("p_startedAt", LocalDateTime.now());

        QuestionDto q = practiceService.generateQuestion();
        session.setAttribute("p_answer", q.getAnswer());

        model.addAttribute("question", q.getText());
        model.addAttribute("index", 1);
        model.addAttribute("total", total);

        return "practice";
    }

    // ================== SUBMIT ANSWER ==================
    @PostMapping("/practice/answer")
    public String answer(@RequestParam("userAnswer") int userAnswer,
                         Model model,
                         HttpSession session,
                         Authentication auth) {

        int total = (int) session.getAttribute("p_total");
        int index = (int) session.getAttribute("p_index");
        int correct = (int) session.getAttribute("p_correct");
        int rightAnswer = (int) session.getAttribute("p_answer");

        if (userAnswer == rightAnswer) correct++;

        index++;

        session.setAttribute("p_index", index);
        session.setAttribute("p_correct", correct);

        // ✅ FINISH => SAVE HISTORY
        if (index >= total) {
            String username = auth.getName();
            LocalDateTime startedAt = (LocalDateTime) session.getAttribute("p_startedAt");

            PracticeHistory saved = practiceService.saveHistory(username, total, correct, startedAt);

            model.addAttribute("total", total);
            model.addAttribute("correct", correct);
            model.addAttribute("score", saved.getScore());

            return "practice_result";
        }

        // ✅ NEXT QUESTION
        QuestionDto q = practiceService.generateQuestion();
        session.setAttribute("p_answer", q.getAnswer());

        model.addAttribute("question", q.getText());
        model.addAttribute("index", index + 1);
        model.addAttribute("total", total);
        model.addAttribute("correctSoFar", correct);

        return "practice";
    }

    // ================== PRACTICE HISTORY ==================
    @GetMapping("/practice/history")
    public String history(Model model, Authentication auth) {
        String username = auth.getName();
        List<PracticeHistory> list = practiceService.getPracticeHistory(username);
        model.addAttribute("items", list);
        return "practice_history";
    }
}
