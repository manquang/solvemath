package com.example.solvemath.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.solvemath.models.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDAO {
    @Insert
    void insert(ChatMessage chatMessage);

    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    List<ChatMessage> getAllMessages();

    @Query("DELETE FROM chat_message")
    void clearAll();

    @Query("SELECT * FROM chat_message WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesBySessionId(int sessionId);
}
