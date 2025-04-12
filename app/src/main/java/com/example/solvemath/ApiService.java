package com.example.solvemath;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.example.solvemath.models.ChatMessage;

public interface ApiService {
    @POST("solve")
    Call<ChatMessage> sendQuestion(@Body ChatMessage message);
}