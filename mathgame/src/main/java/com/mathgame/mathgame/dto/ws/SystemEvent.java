package com.mathgame.mathgame.dto.ws;

public class SystemEvent {
    private String type; // IDLE | QUEUING | CONFIRM | PLAYING | RESULT
    private String message;

    public SystemEvent() {}
    public SystemEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
