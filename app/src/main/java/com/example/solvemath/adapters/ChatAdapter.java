package com.example.solvemath.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.solvemath.activities.WebviewActivity;
import com.example.solvemath.databinding.ItemContainerAnswerBinding;
import com.example.solvemath.databinding.ItemContainerQuestionBinding;
import com.example.solvemath.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    public static final int VIEW_TYPE_USER_MESSAGE = 1;
    public static final int VIEW_TYPE_AI_MESSAGE = 2;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            return new QuestionMessageViewHolder(ItemContainerQuestionBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new AnswerMessageViewHolder(ItemContainerAnswerBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_USER_MESSAGE) {
            ((QuestionMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((AnswerMessageViewHolder) holder).setData(chatMessages.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).isUser()) {
            return VIEW_TYPE_USER_MESSAGE;
        } else {
            return VIEW_TYPE_AI_MESSAGE;
        }
    }

    static class QuestionMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerQuestionBinding binding;

        public QuestionMessageViewHolder(ItemContainerQuestionBinding itemContainerQuestionBinding) {
            super(itemContainerQuestionBinding.getRoot());
            binding = itemContainerQuestionBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.getType() == ChatMessage.Type.TEXT) {
                binding.textUserMessage.setVisibility(View.VISIBLE);
                binding.imageUserMessage.setVisibility(View.GONE);
                binding.textUserMessage.setText(chatMessage.getContent());
            } else if (chatMessage.getType() == ChatMessage.Type.IMAGE) {
                binding.textUserMessage.setVisibility(View.GONE);
                binding.imageUserMessage.setVisibility(View.VISIBLE);
                Glide.with(binding.getRoot()).load(chatMessage.getContent()).into(binding.imageUserMessage);
            }
        }
    }

    static class AnswerMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerAnswerBinding binding;

        public AnswerMessageViewHolder(ItemContainerAnswerBinding itemContainerAnswerBinding) {
            super(itemContainerAnswerBinding.getRoot());
            binding = itemContainerAnswerBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.getType() == ChatMessage.Type.TEXT) {
                binding.textMessage.setText(chatMessage.getContent());
            } else if (chatMessage.getType() == ChatMessage.Type.HTML) {
                String fullText = "Xem toàn bộ lời giải. Nhấp vào đây.";
                SpannableString spannable = new SpannableString(fullText);
                int start = fullText.indexOf("đây");
                int end = start + "đây".length();
                spannable.setSpan(new UnderlineSpan(), start,  end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Gạch chân chữ "đây"
                spannable.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Intent intent = new Intent(binding.getRoot().getContext(), WebviewActivity.class);
                        intent.putExtra("html", chatMessage.getContent());
                        binding.getRoot().getContext().startActivity(intent);
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Biến "đây" thành đoạn có thể nhấn

                binding.textMessage.setText(spannable);
                binding.textMessage.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
}
