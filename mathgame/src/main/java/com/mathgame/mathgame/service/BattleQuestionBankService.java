package com.mathgame.mathgame.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathgame.mathgame.dto.battle.BattleQuestionItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class BattleQuestionBankService {

    private final ObjectMapper mapper = new ObjectMapper();
    private List<BattleQuestionItem> cached;

    public List<BattleQuestionItem> getAll() {
        if (cached != null) return cached;

        try (InputStream is = new ClassPathResource("questions/battle_questions.json").getInputStream()) {
            cached = mapper.readValue(is, new TypeReference<List<BattleQuestionItem>>() {});
            return cached;
        } catch (Exception e) {
            throw new RuntimeException("Không đọc được questions/battle_questions.json", e);
        }
    }
}
