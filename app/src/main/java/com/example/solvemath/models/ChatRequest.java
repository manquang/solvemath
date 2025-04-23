package com.example.solvemath.models;

import java.util.List;

public class ChatRequest {
    public String model;
    public List<Message> messages;
    public Double temperature;
    public Double top_p;
    public Integer max_tokens;


    public ChatRequest(String model, List<Message> messages, Double temperature, Double top_p, Integer max_tokens) {
        this.max_tokens = max_tokens;
        this.messages = messages;
        this.model = model;
        this.temperature = temperature;
        this.top_p = top_p;
    }

    public static class Message {
        public String role;
        public List<Content> content;

        public Message(String role, List<Content> content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class Content {
        public String type;
        public ImageUrl image_url;
        public String text;


        // Constructors hỗ trợ
        public static Content fromText(String text) {
            Content c = new Content();
            c.type = "text";
            c.text = text;
            return c;
        }

        public static Content fromImageUrl(String imageUrl) {
            Content c = new Content();
            c.type = "image_url";
            c.image_url = new ImageUrl(imageUrl);
            return c;
        }
    }

    public static class ImageUrl {
        public String url;

        public ImageUrl(String url) {
            this.url = url;
        }
    }
}
