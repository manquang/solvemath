package com.example.solvemath.activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.solvemath.R;
import com.example.solvemath.databinding.ActivityWebviewBinding;

public class WebviewActivity extends AppCompatActivity {
    private ActivityWebviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String latexContent = getIntent().getStringExtra("latex");
        String html = "<html><head>" +
                "<script type='text/javascript' async " +
                "src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js'></script>" +
                "</head><body style='padding: 16px; font-size: 18px'>" +
                latexContent +
                "</body></html>";

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient());
        binding.webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

        binding.btnBack.setOnClickListener(v -> finish());
    }
}