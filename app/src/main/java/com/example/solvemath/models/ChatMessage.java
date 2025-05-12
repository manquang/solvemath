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

    private String publicID;

    public ChatMessage(boolean isUser, Type type, String content,String publicID) {
        this.content = content;
        this.type = type;
        this.isUser = isUser;
        this.publicID = publicID;
    }

    public String getPublicID() {
        return publicID;
    }

    public void setPublicID(String publicID) {
        this.publicID = publicID;
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
