package com.example.solvemath.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.solvemath.adapters.HistoryAdapter;
import com.example.solvemath.adapters.UpdateRec;
import com.example.solvemath.database.ChatDatabase;
import com.example.solvemath.databinding.FragmentHistoryBinding;
import com.example.solvemath.models.ChatSession;

import java.util.List;


public class HistoryFragment extends Fragment implements UpdateRec<ChatSession> {

    private FragmentHistoryBinding binding;
    private HistoryAdapter adapter;
    private List<ChatSession> chatSessions;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top * 3, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        chatSessions = ChatDatabase.getInstance(getContext()).chatSessionDao().getAllSessions();
        adapter = new HistoryAdapter(chatSessions, this);
        binding.rcvHistoryChat.setAdapter(adapter);
        callBack(chatSessions);
        return binding.getRoot();
    }


    @Override
    public void callBack(List<ChatSession> list) {
        if (list.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rcvHistoryChat.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rcvHistoryChat.setVisibility(View.VISIBLE);
        }
    }
}