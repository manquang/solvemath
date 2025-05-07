package com.example.solvemath;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import com.example.solvemath.models.ChatMessage;
import com.example.solvemath.models.ChatRequest;
import com.example.solvemath.models.ChatResponse;

public interface ApiService {
//    @POST("chat/completions")
//    Call<ChatResponse> askAI(@Body ChatRequest request);
    @POST("/qa")
    Call<ChatResponse> sendQA(@Body ChatRequest request);
}