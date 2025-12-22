package com.mathgame.mathgame.dto.ws;

public class AnswerRequest {
    private String matchId;
    private int index;
    private String choice; // A/B/C/D

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
}
