package com.example.solvemath.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.solvemath.activities.ChatActivity;
import com.example.solvemath.database.ChatDatabase;
import com.example.solvemath.databinding.ItemHistoryBinding;
import com.example.solvemath.models.ChatSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final List<ChatSession> chatSessions;
    UpdateRec updateRec;

    public HistoryAdapter(List<ChatSession> chatSessions, UpdateRec updateRec) {
        this.chatSessions = chatSessions;
        this.updateRec = updateRec;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryViewHolder(ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatSession chatSession = chatSessions.get(position);
        holder.binding.textTitle.setText(chatSession.getTitle());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.binding.textDate.setText(sdf.format(new Date(chatSession.getCreatedAt())));

        holder.binding.textContent.setText(chatSession.getSummary());
        if (chatSession.getImageUrl() == null) {
            holder.binding.imageView.setVisibility(View.GONE);
        } else {
            Glide.with(holder.binding.getRoot()).load(chatSession.getImageUrl()).into(holder.binding.imageView);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(holder.binding.getRoot().getContext(), ChatActivity.class);
            intent.putExtra("sessionId", chatSession.getSessionId());
            holder.binding.getRoot().getContext().startActivity(intent);
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                ChatSession sessionToDelete = chatSessions.get(position);

                // Xóa khỏi database
                ChatDatabase db = ChatDatabase.getInstance(holder.binding.getRoot().getContext());
                db.chatSessionDao().deleteSession(sessionToDelete);

                // Xóa khỏi danh sách hiển thị
                chatSessions.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, chatSessions.size());
                updateRec.callBack(chatSessions);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(chatSessions != null) return chatSessions.size();
        return 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding binding;
        public HistoryViewHolder(ItemHistoryBinding itemHistoryBinding) {
            super(itemHistoryBinding.getRoot());
            binding = itemHistoryBinding;
        }
    }
}
