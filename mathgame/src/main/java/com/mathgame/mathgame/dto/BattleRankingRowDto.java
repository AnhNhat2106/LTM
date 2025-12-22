package com.mathgame.mathgame.dto;

public class BattleRankingRowDto {
    private String username;
    private long totalPoints;
    private long matches;
    private long wins;
    private long draws;
    private long losses;

    public BattleRankingRowDto(String username, long totalPoints, long matches, long wins, long draws, long losses) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.matches = matches;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
    }

    public String getUsername() { return username; }
    public long getTotalPoints() { return totalPoints; }
    public long getMatches() { return matches; }
    public long getWins() { return wins; }
    public long getDraws() { return draws; }
    public long getLosses() { return losses; }
}
