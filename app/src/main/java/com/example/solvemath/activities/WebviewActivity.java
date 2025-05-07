package com.example.solvemath.activities;

import android.net.Uri;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

        String htmlURL = getIntent().getStringExtra("html");

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient());
        display(htmlURL);

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void display(String htmlURL) {
        String fileName = Uri.parse(htmlURL).getLastPathSegment();
        File file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            try {
                StringBuilder htmlBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        openFileInput(fileName), StandardCharsets.UTF_8
                ));
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlBuilder.append(line).append("\n");
                }
                reader.close();
                String htmlContent = htmlBuilder.toString();

                binding.webView.loadDataWithBaseURL(
                        null,
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Thread(() -> {
                try {
                    URL url = new URL(htmlURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                    );

                    StringBuilder htmlBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        htmlBuilder.append(line).append("\n");
                    }

                    reader.close();
                    connection.disconnect();

                    String htmlContent = htmlBuilder.toString();

                    // Lưu file vào bộ nhớ
                    try (FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE)) {
                        fos.write(htmlContent.getBytes(StandardCharsets.UTF_8));
                    }

                    // Hiển thị
                    runOnUiThread(() -> binding.webView.loadDataWithBaseURL(
                            null,
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                    ));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}