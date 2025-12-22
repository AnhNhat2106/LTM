package com.mathgame.mathgame.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "battle_results")
public class BattleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dùng để gom 2 dòng (2 người chơi) vào cùng 1 trận.
     * Ví dụ: UUID string.
     */
    @Column(name = "match_id", nullable = false, length = 36)
    private String matchId;

    // người chơi (tài khoản hiện tại)
    @Column(nullable = false, length = 100)
    private String username;

    // đối thủ (username đối thủ)
    @Column(nullable = false, length = 100)
    private String opponent;

    // điểm số của mình trong trận (số câu đúng / score realtime)
    @Column(name = "my_score", nullable = false)
    private int myScore;

    // điểm số của đối thủ trong trận
    @Column(name = "opponent_score", nullable = false)
    private int opponentScore;

    // WIN / LOSE / DRAW
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BattleOutcome outcome;

    // điểm battle (+10 / -5 / +5)
    @Column(nullable = false)
    private int scoreDelta;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt = LocalDateTime.now();

    // ===== GET / SET =====

    public Long getId() {
        return id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public int getMyScore() {
        return myScore;
    }

    public void setMyScore(int myScore) {
        this.myScore = myScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }

    public BattleOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(BattleOutcome outcome) {
        this.outcome = outcome;
    }

    public int getScoreDelta() {
        return scoreDelta;
    }

    public void setScoreDelta(int scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }
}
