package com.mathgame.mathgame.service;

import com.mathgame.mathgame.entity.BattleOutcome;
import com.mathgame.mathgame.entity.BattleResult;
import com.mathgame.mathgame.repository.BattleResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BattleResultService {

    private final BattleResultRepository battleResultRepository;

    public BattleResultService(BattleResultRepository battleResultRepository) {
        this.battleResultRepository = battleResultRepository;
    }

    /**
     * Lưu kết quả 1 trận battle
     * Rule:
     * - Thắng: +10
     * - Thua: -5
     * - Hòa: +5
     */
    @Transactional
    public void recordBattle(
            String player1,
            String player2,
            int score1,
            int score2
    ) {
        String matchId = UUID.randomUUID().toString();

        BattleOutcome p1Outcome;
        BattleOutcome p2Outcome;
        int p1Delta;
        int p2Delta;

        if (score1 > score2) {
            p1Outcome = BattleOutcome.WIN;
            p2Outcome = BattleOutcome.LOSE;
            p1Delta = 10;
            p2Delta = -5;
        } else if (score1 < score2) {
            p1Outcome = BattleOutcome.LOSE;
            p2Outcome = BattleOutcome.WIN;
            p1Delta = -5;
            p2Delta = 10;
        } else {
            p1Outcome = BattleOutcome.DRAW;
            p2Outcome = BattleOutcome.DRAW;
            p1Delta = 5;
            p2Delta = 5;
        }

        BattleResult r1 = new BattleResult();
        r1.setMatchId(matchId);
        r1.setUsername(player1);
        r1.setOpponent(player2);
        r1.setMyScore(score1);
        r1.setOpponentScore(score2);
        r1.setOutcome(p1Outcome);
        r1.setScoreDelta(p1Delta);

        BattleResult r2 = new BattleResult();
        r2.setMatchId(matchId);
        r2.setUsername(player2);
        r2.setOpponent(player1);
        r2.setMyScore(score2);
        r2.setOpponentScore(score1);
        r2.setOutcome(p2Outcome);
        r2.setScoreDelta(p2Delta);

        battleResultRepository.save(r1);
        battleResultRepository.save(r2);
    }
}
