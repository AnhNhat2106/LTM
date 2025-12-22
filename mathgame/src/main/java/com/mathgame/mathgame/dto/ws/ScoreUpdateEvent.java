package com.mathgame.mathgame.dto.ws;

public class ScoreUpdateEvent {
    private String matchId;
    private String player1;
    private String player2;
    private int score1;
    private int score2;
    private int remainSec;

    public ScoreUpdateEvent() {}

    public ScoreUpdateEvent(String matchId, String player1, String player2, int score1, int score2, int remainSec) {
        this.matchId = matchId;
        this.player1 = player1;
        this.player2 = player2;
        this.score1 = score1;
        this.score2 = score2;
        this.remainSec = remainSec;
    }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }

    public int getScore1() { return score1; }
    public void setScore1(int score1) { this.score1 = score1; }

    public int getScore2() { return score2; }
    public void setScore2(int score2) { this.score2 = score2; }

    public int getRemainSec() { return remainSec; }
    public void setRemainSec(int remainSec) { this.remainSec = remainSec; }
}
