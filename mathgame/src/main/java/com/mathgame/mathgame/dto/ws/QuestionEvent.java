package com.mathgame.mathgame.dto.ws;

public class QuestionEvent {
    private String matchId;
    private int index;
    private int total;
    private String questionText;
    private String[] options;
    private int remainSec;

    public QuestionEvent() {}

    public QuestionEvent(String matchId, int index, int total, String questionText, String[] options, int remainSec) {
        this.matchId = matchId;
        this.index = index;
        this.total = total;
        this.questionText = questionText;
        this.options = options;
        this.remainSec = remainSec;
    }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String[] getOptions() { return options; }
    public void setOptions(String[] options) { this.options = options; }

    public int getRemainSec() { return remainSec; }
    public void setRemainSec(int remainSec) { this.remainSec = remainSec; }
}
