package com.example.solvemath.activities;

import static com.example.solvemath.SolveMathApp.getRetrofitInstance;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.solvemath.ApiService;
import com.example.solvemath.adapters.ChatAdapter;
import com.example.solvemath.databinding.ActivityChatBinding;
import com.example.solvemath.models.ChatMessage;
import com.example.solvemath.models.ChatRequest;
import com.example.solvemath.models.ChatResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        setupEdgeToEdge();
        initUI();

        apiService = getRetrofitInstance().create(ApiService.class);
        sendQuestion(ChatMessage.Type.IMAGE);
    }

    private void setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });
    }

    private void initUI() {
        adapter = new ChatAdapter(chatMessages);
        binding.chatRecycleView.setAdapter(adapter);

        binding.back.setOnClickListener(v -> finish());
        binding.layoutSend.setOnClickListener(v -> sendQuestion(ChatMessage.Type.TEXT));
    }

    private void sendQuestion(ChatMessage.Type type) {
        String imageUrl = getIntent().getStringExtra("url");
        String userInput = binding.questionInput.getText().toString().trim();

        if (imageUrl == null && userInput.isEmpty()) return;

        ChatMessage userMessage = new ChatMessage(true, type, type == ChatMessage.Type.IMAGE ? imageUrl : userInput);
        addMessage(userMessage);

        if (type == ChatMessage.Type.IMAGE) {
            ArrayList<ChatRequest.Content> contents = new ArrayList<>();
            contents.add(ChatRequest.Content.fromText("Hãy trích xuất lại nội dung văn bản trong ảnh sau, không cần giải."));
            contents.add(ChatRequest.Content.fromImageUrl(imageUrl));
            ChatRequest request = new ChatRequest("Qwen/Qwen2.5-VL-72B-Instruct",
                    List.of(new ChatRequest.Message("user",contents)), 0.3,0.9, 1024);

            apiService.askAI(request).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String reply = response.body().choices.get(0).message.content;
                        ChatMessage extractedMessage = new ChatMessage(true, ChatMessage.Type.TEXT, reply);
                        addMessage(extractedMessage);
                        sendToDeepSeek(reply);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                    ChatMessage error = new ChatMessage(true, ChatMessage.Type.TEXT, "Lỗi nhận diện ảnh: " + t.getMessage());
                    addMessage(error);
                }
            });
            getIntent().removeExtra("url");
        } else {
            binding.questionInput.setText(null);
            sendToDeepSeek(userInput);
        }
    }


    private void sendToDeepSeek(String content) {
        ChatRequest request = new ChatRequest("deepseek-ai/DeepSeek-V3",
                List.of(new ChatRequest.Message("user",
                        List.of(ChatRequest.Content.fromText(content)))), 0.3, 0.9, 1024);

        apiService.askAI(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().choices.get(0).message.content;

                    ChatMessage.Type type = containsLatex(reply) ? ChatMessage.Type.LATEX : ChatMessage.Type.TEXT;
                    addMessage(new ChatMessage(false, type, reply));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                addMessage(new ChatMessage(false, ChatMessage.Type.TEXT, "Lỗi gửi tới DeepSeek: " + t.getMessage()));
            }
        });
    }

    private boolean containsLatex(String content) {
        return content.contains("\\(") || content.contains("\\[") || content.contains("$$");
    }

    private void addMessage(ChatMessage message) {
        chatMessages.add(message);
        adapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
    }

}
