package com.mathgame.mathgame.dto.ws;

public class ResultEvent {
    private String matchId;
    private String player1;
    private String player2;
    private int score1;
    private int score2;
    private int myDelta; // điểm battle (+10/-5/+5) của user nhận event

    public ResultEvent() {}

    public ResultEvent(String matchId, String player1, String player2, int score1, int score2, int myDelta) {
        this.matchId = matchId;
        this.player1 = player1;
        this.player2 = player2;
        this.score1 = score1;
        this.score2 = score2;
        this.myDelta = myDelta;
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

    public int getMyDelta() { return myDelta; }
    public void setMyDelta(int myDelta) { this.myDelta = myDelta; }
}
