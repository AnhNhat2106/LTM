package com.mathgame.mathgame.dto;

public class PracticeQuestion {
    private Long id;
    private String text;
    private int answer;
    private String level;

    public PracticeQuestion() {}

    public PracticeQuestion(Long id, String text, int answer) {
        this.id = id;
        this.text = text;
        this.answer = answer;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getAnswer() { return answer; }
    public void setAnswer(int answer) { this.answer = answer; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
