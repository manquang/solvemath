package com.example.solvemath.activities;

import static com.example.solvemath.SolveMathApp.getRetrofitInstance;

import android.os.Bundle;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.solvemath.ApiService;
import com.example.solvemath.adapters.ChatAdapter;
import com.example.solvemath.databinding.ActivityChatBinding;
import com.example.solvemath.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity{
    private ApiService apiService;
    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        Window window = getWindow();
        WindowCompat.getInsetsController(window, window.getDecorView())
                .setAppearanceLightStatusBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        binding.back.setOnClickListener(v -> finish());
        binding.layoutSend.setOnClickListener(v -> sendQuestion(ChatMessage.Type.TEXT));
    }

    private void init() {
        chatMessages = new ArrayList<>();
        adapter = new ChatAdapter(chatMessages);
        binding.chatRecycleView.setAdapter(adapter);

        apiService = getRetrofitInstance().create(ApiService.class);
        sendQuestion(ChatMessage.Type.IMAGE);
    }

    private void sendQuestion(ChatMessage.Type type) {
        String imageUrl = getIntent().getStringExtra("url");
        String textMessage = binding.questionInput.getText().toString().trim();
        if (imageUrl == null && textMessage.isEmpty()) {
            return;
        }

        ChatMessage userMessage;
        if (type == ChatMessage.Type.IMAGE) {
            userMessage = new ChatMessage(true, type, imageUrl);
            getIntent().removeExtra("url");

        } else {
            userMessage = new ChatMessage(true, type, textMessage);
            binding.questionInput.setText(null);

        }
        chatMessages.add(userMessage);
        adapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
        // Gửi lên server
        apiService.sendQuestion(userMessage).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatMessage reply = new ChatMessage(false, ChatMessage.Type.HTML, response.body().getContent());
                    chatMessages.add(reply);
                    adapter.notifyItemInserted(chatMessages.size() - 1);
                    binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                // Xử lý lỗi
            }
        });
    }

}