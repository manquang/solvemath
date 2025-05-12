package com.example.solvemath.activities;

import static com.example.solvemath.SolveMathApp.getRetrofitInstance;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.solvemath.ApiService;
import com.example.solvemath.R;
import com.example.solvemath.adapters.ChatAdapter;
import com.example.solvemath.database.ChatDatabase;
import com.example.solvemath.databinding.ActivityChatBinding;
import com.example.solvemath.models.ChatMessage;
import com.example.solvemath.models.ChatRequest;
import com.example.solvemath.models.ChatResponse;
import com.example.solvemath.models.ChatSession;
import com.example.solvemath.models.OCRResponse;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ApiService apiService;
    private int sessionId = -1;


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
        sessionId = getIntent().getIntExtra("sessionId", -1);
        if (sessionId != -1) {
            loadMessagesFromSession(sessionId);
        }

        adapter = new ChatAdapter(chatMessages);
        binding.chatRecycleView.setAdapter(adapter);

        binding.back.setOnClickListener(v -> finish());
        binding.layoutSend.setOnClickListener(v -> sendQuestion(ChatMessage.Type.TEXT));
    }

    private void loadMessagesFromSession(int sessionId) {
        new Thread(() -> {
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            List<ChatMessage> messages = db.chatMessageDao().getMessagesBySessionId(sessionId);
            runOnUiThread(() -> {
                chatMessages.clear();
                chatMessages.addAll(messages);
                adapter.notifyDataSetChanged();
                binding.chatRecycleView.scrollToPosition(chatMessages.size() - 1);
            });
        }).start();
    }

    private void sendQuestion(ChatMessage.Type type) {
        String imageUrl = getIntent().getStringExtra("img_url");
        String publicImageId = getIntent().getStringExtra("public_id");
        String userInput = binding.questionInput.getText().toString().trim();

        if (imageUrl == null && userInput.isEmpty()) return;

        ChatMessage userMessage = new ChatMessage(true, type,
                type == ChatMessage.Type.IMAGE ? imageUrl : userInput, publicImageId);

        createSession(userInput, imageUrl, type, () -> addMessage(userMessage));

        ChatRequest ocrRequest = new ChatRequest();
        ChatRequest qaRequest = new ChatRequest();
        ocrRequest.setImage_url(imageUrl);

        if (type == ChatMessage.Type.IMAGE) {
            apiService.extractOCR(ocrRequest).enqueue(new Callback<OCRResponse>() {
                @Override
                public void onResponse(Call<OCRResponse> call, Response<OCRResponse> response) {
                    String extractedText = "";
                    if (response.isSuccessful() && response.body() != null) {
                        extractedText = response.body().getOcrText();
                    }

                    addMessage(new ChatMessage(true, ChatMessage.Type.TEXT, "Bài toán sau khi trích xuất được: \n" + extractedText, null));
                    qaRequest.setInput(extractedText);
                    sendQARequest(qaRequest);
                }

                @Override
                public void onFailure(Call<OCRResponse> call, Throwable t) {
                    ChatMessage error = new ChatMessage(false, ChatMessage.Type.TEXT,
                            "Lỗi OCR: " + t.getMessage(), null);
                    addMessage(error);
                }
            });

        } else {
            qaRequest.setInput(userInput);
            binding.questionInput.setText(null);
            sendQARequest(qaRequest);
        }
    }



    private void createSession(String title, String imageUrl, ChatMessage.Type type, Runnable afterSessionCreated) {
        if (sessionId == -1) {
            String sessionImageUrl = type == ChatMessage.Type.IMAGE ? imageUrl : null;

            long createdAt = System.currentTimeMillis();
            ChatSession session = new ChatSession(sessionImageUrl, "", title, createdAt);

            new Thread(() -> {
                ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
                long id = db.chatSessionDao().insert(session);
                sessionId = (int) id;
                runOnUiThread(afterSessionCreated);
            }).start();
        } else {
            afterSessionCreated.run();
        }
    }

    private void sendQARequest(ChatRequest request) {
        apiService.sendQA(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().getSummary();
                    String fileUrl = response.body().getFileUrl();
                    String answerText = response.body().getResult();
                    String publicID = response.body().getPublicID();

                    updateSession(request.getInput(), summary);

                    if (answerText != null && !answerText.trim().isEmpty()) {
                        ChatMessage textMessage = new ChatMessage(false, ChatMessage.Type.TEXT, answerText, null);
                        addMessage(textMessage);
                    }

                    if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                        ChatMessage htmlMessage = new ChatMessage(false, ChatMessage.Type.HTML, fileUrl, publicID);
                        addMessage(htmlMessage);
                    }
                } else {
                    ChatMessage error = new ChatMessage(false, ChatMessage.Type.TEXT,
                            "Lỗi nhận câu trả lời.", null);
                    addMessage(error);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                ChatMessage error = new ChatMessage(false, ChatMessage.Type.TEXT,
                        "Lỗi: " + t.getMessage(), null);
                addMessage(error);
            }
        });
    }



    private void updateSession(String title, String summary) {
        new Thread(() -> {
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            ChatSession session = db.chatSessionDao().getSessionById(sessionId);
            if (session != null) {
                session.setTitle(title);
                session.setSummary(summary);
                db.chatSessionDao().update(session);
            }
        }).start();
    }

    private void addMessage(ChatMessage message) {
        chatMessages.add(message);
        adapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
        saveMessageToRoom(message);
    }

    private void saveMessageToRoom(ChatMessage message) {
        new Thread(() -> {
            if (sessionId != -1) {
                ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
                message.setSessionId(sessionId);
                db.chatMessageDao().insert(message);
            }
        }).start();
    }
}
