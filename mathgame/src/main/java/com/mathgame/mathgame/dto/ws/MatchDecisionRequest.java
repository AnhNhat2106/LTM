package com.mathgame.mathgame.dto.ws;

public class MatchDecisionRequest {
    private String matchId;

    public MatchDecisionRequest() {}
    public MatchDecisionRequest(String matchId) { this.matchId = matchId; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
}
