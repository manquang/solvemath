package com.example.solvemath.models;

public class ChatMessage {
    public enum Type {
        TEXT, IMAGE, HTML, LATEX
    }
    private final String content;
    private final Type type;
    private final boolean isUser;

    public ChatMessage(boolean isUser, Type type, String content) {
        this.isUser = isUser;
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }
    public Type getType() {
        return type;
    }


    public boolean isUser() {
        return isUser;
    }

}
