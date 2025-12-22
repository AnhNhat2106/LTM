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

@Service
public class PracticeService {

    private final PracticeHistoryRepository historyRepo;
    private final Random random = new Random();

    // ✅ danh sách câu hỏi load từ JSON
    private List<PracticeQuestion> bank = Collections.emptyList();

    public PracticeService(PracticeHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
        loadQuestionBank(); // load ngay khi service khởi tạo
    }

    // ====== Load câu hỏi từ file JSON trong resources ======
    private void loadQuestionBank() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource res = new ClassPathResource("questions/practice_questions.json");
            try (InputStream is = res.getInputStream()) {
                this.bank = mapper.readValue(is, new TypeReference<List<PracticeQuestion>>() {});
            }
            System.out.println("✅ Loaded practice_questions.json: " + bank.size() + " questions");
        } catch (Exception e) {
            this.bank = Collections.emptyList();
            System.err.println("❌ Không load được practice_questions.json: " + e.getMessage());
        }
    }

    // ====== Generate câu hỏi từ bank ======
    public QuestionDto generateQuestion() {
        if (bank == null || bank.isEmpty()) {
            throw new RuntimeException("Question bank rỗng! Kiểm tra file: src/main/resources/questions/practice_questions.json");
        }
        PracticeQuestion q = bank.get(random.nextInt(bank.size()));
        return new QuestionDto(q.getText(), q.getAnswer());
    }

    // ====== Tính điểm ======
    public int calcScore(int correctAnswers) {
        return correctAnswers * 10;
    }

    // ====== Lưu lịch sử luyện tập ======
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

    // ====== Lấy lịch sử luyện tập theo user ======
    public List<PracticeHistory> getPracticeHistory(String username) {
        return historyRepo.findByUsernameOrderByIdDesc(username);
    }
}
