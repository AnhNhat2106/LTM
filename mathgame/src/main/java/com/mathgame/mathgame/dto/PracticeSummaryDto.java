package com.mathgame.mathgame.dto;

public class PracticeSummaryDto {
    private long totalScore;
    private long totalPlays;
    private long totalCorrect;

    public PracticeSummaryDto(long totalScore, long totalPlays, long totalCorrect) {
        this.totalScore = totalScore;
        this.totalPlays = totalPlays;
        this.totalCorrect = totalCorrect;
    }

    public long getTotalScore() { return totalScore; }
    public long getTotalPlays() { return totalPlays; }
    public long getTotalCorrect() { return totalCorrect; }
}
