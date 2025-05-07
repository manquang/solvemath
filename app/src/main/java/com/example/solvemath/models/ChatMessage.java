package com.example.solvemath.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "chat_message",
        foreignKeys = @ForeignKey(
                entity = ChatSession.class,
                parentColumns = "sessionId",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE
        )
)
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    public enum Type {
        TEXT, IMAGE, HTML
    }
    private int sessionId;
    private final String content;
    private final Type type;
    public long timestamp;
    private final boolean isUser;

    public ChatMessage(boolean isUser, Type type, String content) {
        this.isUser = isUser;
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
