package com.example.solvemath.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.solvemath.models.ChatSession;

import java.util.List;

@Dao
public interface ChatSessionDAO {
    @Insert
    long insert(ChatSession session);

    @Update
    void update(ChatSession session);
    @Delete
    void deleteSession(ChatSession session);

    @Query("SELECT * FROM chat_session ORDER BY createdAt DESC")
    List<ChatSession> getAllSessions();

    @Query("SELECT * FROM chat_session WHERE sessionId = :id LIMIT 1")
    ChatSession getSessionById(int id);

}
