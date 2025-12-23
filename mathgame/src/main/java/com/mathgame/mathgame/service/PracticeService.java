package com.mathgame.mathgame.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathgame.mathgame.dto.PracticeQuestion;
import com.mathgame.mathgame.dto.QuestionDto;
import com.mathgame.mathgame.entity.PracticeHistory;
import com.mathgame.mathgame.repository.PracticeHistoryRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class PracticeService {

    private final PracticeHistoryRepository historyRepo;
    private final Random random = new Random();

    private List<PracticeQuestion> bank = Collections.emptyList();

    public PracticeService(PracticeHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
        loadQuestionBank();
    }

    private void loadQuestionBank() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource res = new ClassPathResource("questions/practice_questions.json");
            try (InputStream is = res.getInputStream()) {
                this.bank = mapper.readValue(is, new TypeReference<List<PracticeQuestion>>() {});
            }
            System.out.println("Loaded practice_questions.json: " + bank.size() + " questions");
        } catch (Exception e) {
            this.bank = Collections.emptyList();
            System.err.println("Không load được practice_questions.json: " + e.getMessage());
        }
    }

    public QuestionDto generateQuestion() {
        return generateQuestion(null);
    }

    public QuestionDto generateQuestion(String level) {
        if (bank == null || bank.isEmpty()) {
            throw new RuntimeException("Question bank rỗng! Kiểm tra file: src/main/resources/questions/practice_questions.json");
        }

        List<PracticeQuestion> pool = getPool(level);
        if (pool.isEmpty()) {
            pool = bank;
        }

        PracticeQuestion q = pool.get(random.nextInt(pool.size()));
        return new QuestionDto(q.getText(), q.getAnswer());
    }

    public PracticeQuestion pickQuestion(String level, List<Integer> usedIds) {
        if (bank == null || bank.isEmpty()) {
            return null;
        }

        List<PracticeQuestion> pool = getPool(level);
        if (pool.isEmpty()) {
            return null;
        }

        List<PracticeQuestion> available = pool;
        if (usedIds != null && !usedIds.isEmpty()) {
            available = pool.stream()
                    .filter(q -> q.getId() != null && !usedIds.contains(q.getId().intValue()))
                    .collect(Collectors.toList());
        }

        if (available.isEmpty()) {
            return null;
        }

        return available.get(random.nextInt(available.size()));
    }

    public int countByLevel(String level) {
        if (bank == null || bank.isEmpty()) {
            return 0;
        }
        return getPool(level).size();
    }

    private List<PracticeQuestion> getPool(String level) {
        if (level == null || level.isBlank()) {
            return bank;
        }
        String target = level.trim().toLowerCase();
        return bank.stream()
                .filter(q -> q.getLevel() != null && q.getLevel().trim().equalsIgnoreCase(target))
                .collect(Collectors.toList());
    }

    public int calcScore(int correctAnswers) {
        return correctAnswers * 10;
    }

    public PracticeHistory saveHistory(String username, int total, int correct, LocalDateTime startedAt) {
        PracticeHistory h = new PracticeHistory();
        h.setUsername(username);
        h.setTotalQuestions(total);
        h.setCorrectAnswers(correct);
        h.setScore(calcScore(correct));
        h.setStartedAt(startedAt);
        h.setEndedAt(LocalDateTime.now());
        return historyRepo.save(h);
    }

    public List<PracticeHistory> getPracticeHistory(String username) {
        return historyRepo.findByUsernameOrderByIdDesc(username);
    }
}
