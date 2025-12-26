package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.dto.PracticeQuestion;
import com.mathgame.mathgame.entity.PracticeHistory;
import com.mathgame.mathgame.service.PracticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/practice")
    public String practice(Model model,
                           HttpSession session,
                           @RequestParam(value = "level", required = false) String level) {

        String normalized = normalizeLevel(level);
        if (normalized == null) {
            return "practice_select";
        }

        int totalAvailable = practiceService.countByLevel(normalized);
        if (totalAvailable == 0) {
            model.addAttribute("error", "Chưa có câu hỏi cho mức độ này.");
            return "practice_select";
        }

        int total = Math.min(10, totalAvailable);

        session.setAttribute("p_level", normalized);
        session.setAttribute("p_total", total);
        session.setAttribute("p_index", 0);
        session.setAttribute("p_correct", 0);
        session.setAttribute("p_startedAt", LocalDateTime.now());
        session.setAttribute("p_used", new ArrayList<Integer>());

        PracticeQuestion q = pickNext(session, normalized);
        if (q == null) {
            model.addAttribute("error", "Không tìm thấy câu hỏi.");
            return "practice_select";
        }

        model.addAttribute("question", q.getText());
        model.addAttribute("index", 1);
        model.addAttribute("total", total);
        model.addAttribute("levelLabel", levelLabel(normalized));
        model.addAttribute("options", session.getAttribute("p_options"));
        model.addAttribute("letters", letters());

        return "practice";
    }

    @PostMapping("/practice/answer")
    public String answer(@RequestParam("choice") String choice,
                         Model model,
                         HttpSession session,
                         Authentication auth) {

        int total = (int) session.getAttribute("p_total");
        int index = (int) session.getAttribute("p_index");
        int correct = (int) session.getAttribute("p_correct");
        String level = (String) session.getAttribute("p_level");
        String correctChoice = (String) session.getAttribute("p_correctChoice");

        if (correctChoice != null && correctChoice.equalsIgnoreCase(choice)) {
            correct++;
        }

        index++;

        session.setAttribute("p_index", index);
        session.setAttribute("p_correct", correct);

        if (index >= total) {
            String username = auth.getName();
            LocalDateTime startedAt = (LocalDateTime) session.getAttribute("p_startedAt");

            PracticeHistory saved = practiceService.saveHistory(username, total, correct, startedAt);

            model.addAttribute("total", total);
            model.addAttribute("correct", correct);
            model.addAttribute("score", saved.getScore());
            model.addAttribute("levelLabel", levelLabel(level));

            return "practice_result";
        }

        PracticeQuestion q = pickNext(session, level);
        if (q == null) {
            String username = auth.getName();
            LocalDateTime startedAt = (LocalDateTime) session.getAttribute("p_startedAt");
            PracticeHistory saved = practiceService.saveHistory(username, index, correct, startedAt);
            model.addAttribute("total", index);
            model.addAttribute("correct", correct);
            model.addAttribute("score", saved.getScore());
            model.addAttribute("levelLabel", levelLabel(level));
            return "practice_result";
        }

        model.addAttribute("question", q.getText());
        model.addAttribute("index", index + 1);
        model.addAttribute("total", total);
        model.addAttribute("correctSoFar", correct);
        model.addAttribute("levelLabel", levelLabel(level));
        model.addAttribute("options", session.getAttribute("p_options"));
        model.addAttribute("letters", letters());

        return "practice";
    }

    @GetMapping("/practice/history")
    public String history(Model model, Authentication auth) {
        String username = auth.getName();
        List<PracticeHistory> list = practiceService.getPracticeHistory(username);
        model.addAttribute("items", list);
        return "practice_history";
    }

    private PracticeQuestion pickNext(HttpSession session, String level) {
        @SuppressWarnings("unchecked")
        List<Integer> used = (List<Integer>) session.getAttribute("p_used");
        if (used == null) {
            used = new ArrayList<>();
            session.setAttribute("p_used", used);
        }

        PracticeQuestion q = practiceService.pickQuestion(level, used);
        if (q == null) {
            return null;
        }

        if (q.getId() != null) {
            used.add(q.getId().intValue());
            session.setAttribute("p_used", used);
        }

        OptionSet options = genOptions(q.getAnswer());
        session.setAttribute("p_correctChoice", options.correctChoice);
        session.setAttribute("p_options", options.options);

        return q;
    }

    private OptionSet genOptions(int correct) {
        Random rd = new Random();
        List<Integer> values = new ArrayList<>();
        values.add(correct);
        while (values.size() < 4) {
            int cand = correct + (rd.nextInt(9) - 4);
            if (cand < 0 || values.contains(cand)) continue;
            values.add(cand);
        }

        java.util.Collections.shuffle(values);

        OptionSet set = new OptionSet();
        set.options = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            String letter = letters().get(i);
            set.options.add(letter + ") " + values.get(i));
            if (values.get(i) == correct) {
                set.correctChoice = letter;
            }
        }
        return set;
    }

    private List<String> letters() {
        return List.of("A", "B", "C", "D");
    }

    private String normalizeLevel(String level) {
        if (level == null) return null;
        String v = level.trim().toLowerCase();
        return switch (v) {
            case "easy", "medium", "hard" -> v;
            default -> null;
        };
    }

    private String levelLabel(String level) {
        if (level == null) return "-";
        return switch (level) {
            case "easy" -> "Dễ";
            case "medium" -> "Trung bình";
            case "hard" -> "Khó";
            default -> "-";
        };
    }

    private static class OptionSet {
        List<String> options;
        String correctChoice;
    }
}
