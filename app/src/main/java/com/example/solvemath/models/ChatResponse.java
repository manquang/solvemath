package com.example.solvemath.models;

import java.util.List;

public class ChatResponse {
    public List<Choice> choices;

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String role;
        public String content;
    }
}