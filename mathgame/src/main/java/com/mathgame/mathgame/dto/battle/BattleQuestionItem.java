package com.mathgame.mathgame.dto.battle;

public class BattleQuestionItem {
    private int id;
    private String text;
    private int answer;

    public BattleQuestionItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getAnswer() { return answer; }
    public void setAnswer(int answer) { this.answer = answer; }
}
