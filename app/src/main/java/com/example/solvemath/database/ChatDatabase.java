package com.example.solvemath.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.solvemath.models.ChatMessage;
import com.example.solvemath.models.ChatSession;

@Database(entities = {ChatMessage.class, ChatSession.class}, version = 1)
public abstract class ChatDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "chat.db";
    private static ChatDatabase instance;

    public static synchronized ChatDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), ChatDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract ChatMessageDAO chatMessageDao();
    public abstract ChatSessionDAO chatSessionDao();
}
