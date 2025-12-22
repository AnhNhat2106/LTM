package com.mathgame.mathgame.dto;

public class QuestionDto {
    private String text;     // VD: "5 + 3 = ?"
    private int answer;      // đáp án đúng

    public QuestionDto() {}

    public QuestionDto(String text, int answer) {
        this.text = text;
        this.answer = answer;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getAnswer() { return answer; }
    public void setAnswer(int answer) { this.answer = answer; }
}
