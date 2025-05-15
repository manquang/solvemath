package com.example.solvemath.activities;

import static com.example.solvemath.SolveMathApp.getRetrofitInstance;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.solvemath.ApiService;
import com.example.solvemath.adapters.ChatAdapter;
import com.example.solvemath.database.ChatDatabase;
import com.example.solvemath.databinding.ActivityChatBinding;
import com.example.solvemath.models.ChatMessage;
import com.example.solvemath.models.ChatRequest;
import com.example.solvemath.models.ChatResponse;
import com.example.solvemath.models.ChatSession;
import com.example.solvemath.models.OCRResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private Intent intent;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ApiService apiService;
    private ChatMessage loadingMessage;
    private int sessionId = -1;
    private final Executor executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        setupEdgeToEdge();
        intent = getIntent();
        initUI();

        apiService = getRetrofitInstance().create(ApiService.class);
        sendQuestion(ChatMessage.Type.IMAGE);
    }

    @Override
    protected void onNewIntent(@NonNull Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        this.intent = newIntent;
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
        sessionId = intent.getIntExtra("sessionId", -1);
        if (sessionId != -1) {
            loadMessagesFromSession(sessionId);
        }

        adapter = new ChatAdapter(chatMessages, message -> resendMessage(message));
        binding.chatRecycleView.setAdapter(adapter);

        binding.back.setOnClickListener(v -> finish());
        binding.layoutSend.setOnClickListener(v -> sendQuestion(ChatMessage.Type.TEXT));
        binding.layoutCamera.setOnClickListener(v-> startActivity(new Intent(this, CameraActivity.class)));
    }

    private void loadMessagesFromSession(int sessionId) {
        executor.execute(() -> {
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            List<ChatMessage> messages = db.chatMessageDao().getMessagesBySessionId(sessionId);
            runOnUiThread(() -> {
                chatMessages.clear();
                chatMessages.addAll(messages);
                adapter.notifyDataSetChanged();
                binding.chatRecycleView.scrollToPosition(chatMessages.size() - 1);
            });
        });
    }

    private void sendQuestion(ChatMessage.Type type) {
        hideKeyboard();
        String imageUrl = intent.getStringExtra("img_url");
        String publicImageId = intent.getStringExtra("public_id");
        String userInput = binding.questionInput.getText().toString().trim();

        if (imageUrl == null && userInput.isEmpty()) return;

        ChatMessage userMessage = new ChatMessage(true, type,
                type == ChatMessage.Type.IMAGE ? imageUrl : userInput, publicImageId);

        createSession(userInput, imageUrl, type);
        addMessage(userMessage, false);
        ChatRequest qaRequest = new ChatRequest();

        if (type == ChatMessage.Type.IMAGE) {
            sendOCRRequest(imageUrl, userMessage);
        } else {
            qaRequest.setInput(userInput);
            binding.questionInput.setText(null);
            sendQARequest(qaRequest, userMessage);
        }
    }


    private void createSession(String title, String imageUrl, ChatMessage.Type type) {
        if (sessionId == -1) {
            String sessionImageUrl = type == ChatMessage.Type.IMAGE ? imageUrl : null;

            long createdAt = System.currentTimeMillis();
            ChatSession session = new ChatSession(sessionImageUrl, "", title, createdAt);

            executor.execute(() -> {
                ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
                long id = db.chatSessionDao().insert(session);
                sessionId = (int) id;
            });
        }
    }

    private void sendOCRRequest(String imageUrl, ChatMessage userMessage) {
        ChatRequest ocrRequest = new ChatRequest();
        ocrRequest.setImage_url(imageUrl);

        loadingMessage = new ChatMessage(false, ChatMessage.Type.TEXT, "Đang xử lý ảnh...", null);
        addMessage(loadingMessage, true);

        apiService.extractOCR(ocrRequest).enqueue(new Callback<OCRResponse>() {
            @Override
            public void onResponse(Call<OCRResponse> call, Response<OCRResponse> response) {
                removeLoadingMessage();
                updateUserMessageState(userMessage, false);
                String extractedText = "";
                if (response.isSuccessful() && response.body() != null) {
                    extractedText = response.body().getOcrText();
                }

                ChatMessage ocrMessage = new ChatMessage(true, ChatMessage.Type.TEXT, extractedText, null);
                addMessage(ocrMessage, false);

                ChatRequest qaRequest = new ChatRequest();
                qaRequest.setInput(extractedText);
                sendQARequest(qaRequest, ocrMessage);
            }

            @Override
            public void onFailure(Call<OCRResponse> call, Throwable t) {
                removeLoadingMessage();
                updateUserMessageState(userMessage, true);
                ChatMessage error = new ChatMessage(false, ChatMessage.Type.TEXT,
                        "Lỗi OCR: " + t.getMessage(), null);
                addMessage(error, false);
            }
        });
    }

    private void sendQARequest(ChatRequest request, ChatMessage userMessage) {
        loadingMessage = new ChatMessage(false, ChatMessage.Type.TEXT, "Đang xử lý...", null);
        addMessage(loadingMessage, true);
        apiService.sendQA(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                removeLoadingMessage();
                if (response.isSuccessful() && response.body() != null) {
                    updateUserMessageState(userMessage, false);
                    String summary = response.body().getSummary();
                    String fileUrl = response.body().getFileUrl();
                    String answerText = response.body().getResult();
                    String publicID = response.body().getPublicID();

                    updateSession(request.getInput(), summary);

                    if (answerText != null && !answerText.trim().isEmpty()) {
                        ChatMessage textMessage = new ChatMessage(false, ChatMessage.Type.TEXT, answerText, null);
                        addMessage(textMessage, false);
                    }

                    if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                        ChatMessage htmlMessage = new ChatMessage(false, ChatMessage.Type.HTML, fileUrl, publicID);
                        addMessage(htmlMessage, false);
                    }
                } else {
                    updateUserMessageState(userMessage, true);
                    ChatMessage error = new ChatMessage(false, ChatMessage.Type.TEXT,
                            "Lỗi nhận câu trả lời.", null);
                    addMessage(error, false);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                removeLoadingMessage();
                updateUserMessageState(userMessage, true);
                ChatMessage error = new ChatMessage(false, ChatMessage.Type.TEXT,
                        "Lỗi: " + t.getMessage(), null);
                addMessage(error, false);
            }
        });
    }


    private void updateSession(String title, String summary) {
        executor.execute(() -> {
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            ChatSession session = db.chatSessionDao().getSessionById(sessionId);
            if (session != null) {
                session.setTitle(title);
                session.setSummary(summary);
                db.chatSessionDao().update(session);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        View view = binding.questionInput;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void removeLoadingMessage() {
        if (loadingMessage != null && chatMessages.contains(loadingMessage)) {
            int index = chatMessages.indexOf(loadingMessage);
            runOnUiThread(() -> {
                chatMessages.remove(index);
                adapter.notifyItemRemoved(index);
            });
            loadingMessage = null;
        }
    }

    private void updateUserMessageState( ChatMessage message, boolean state) {
        message.setFailed(state);
        int index = chatMessages.indexOf(message);
        if (index != -1) {
            adapter.notifyItemChanged(index);
        }
    }
    private void addMessage(ChatMessage message, boolean tempMessage) {
        chatMessages.add(message);
        adapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
        if (!tempMessage) {
            saveMessageToRoom(message);
        }
    }

    public void resendMessage(ChatMessage message) {
        updateUserMessageState(message, false);
        ChatRequest request = new ChatRequest();
        if (message.getType() == ChatMessage.Type.IMAGE) {
            sendOCRRequest(message.getContent(), message);
        } else {
            request.setInput(message.getContent());
            sendQARequest(request, message);
        }
    }


    private void saveMessageToRoom(ChatMessage message) {
        executor.execute(() -> {
            if (sessionId != -1) {
                ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
                message.setSessionId(sessionId);
                db.chatMessageDao().insert(message);
            }
        });
    }
}
