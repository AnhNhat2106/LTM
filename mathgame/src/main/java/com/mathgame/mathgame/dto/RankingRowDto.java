package com.mathgame.mathgame.dto;

public class RankingRowDto {
    private String username;
    private long totalScore;
    private long totalPlays;
    private long totalCorrect;

    public RankingRowDto(String username, long totalScore, long totalPlays, long totalCorrect) {
        this.username = username;
        this.totalScore = totalScore;
        this.totalPlays = totalPlays;
        this.totalCorrect = totalCorrect;
    }

    public String getUsername() { return username; }
    public long getTotalScore() { return totalScore; }
    public long getTotalPlays() { return totalPlays; }
    public long getTotalCorrect() { return totalCorrect; }
}
