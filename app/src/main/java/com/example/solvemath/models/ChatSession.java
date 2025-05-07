package com.example.solvemath.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_session")
public class ChatSession {
    @PrimaryKey(autoGenerate = true)
    private int sessionId;
    private long createdAt;
    private String title;
    private String summary;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ChatSession(String imageUrl, String summary, String title, long createdAt) {
        this.imageUrl = imageUrl;
        this.summary = summary;
        this.title = title;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}