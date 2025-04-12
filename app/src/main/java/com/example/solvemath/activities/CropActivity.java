package com.example.solvemath.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.canhub.cropper.CropImageView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.solvemath.BuildConfig;
import com.example.solvemath.databinding.ActivityCropBinding;
import com.example.solvemath.utilities.Helper;

import java.util.Map;

public class CropActivity extends AppCompatActivity {
    private ActivityCropBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri == null) {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.imageBack.setOnClickListener(v -> finish());
        binding.rotateButton.setOnClickListener(v -> binding.cropImageView.rotateImage(90));
        binding.cropImageView.setImageUriAsync(imageUri);
        binding.cropImageView.setGuidelines(CropImageView.Guidelines.ON);
        binding.cropButton.setOnClickListener(v->uploadImage());

    }


    private void uploadImage() {
        Bitmap bitmapCropImage = binding.cropImageView.getCroppedImage();
        Uri uri = Helper.getTempImageUri(this, bitmapCropImage);

        MediaManager.get().upload(uri).option("upload_preset", BuildConfig.UPLOAD_PRESET).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d("Cloudinary Quickstart", "Upload start");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Log.d("Cloudinary Quickstart", "Upload progress");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d("Cloudinary Quickstart", "Upload success");
                String url = (String) resultData.get("secure_url");
                Intent intent = new Intent(CropActivity.this, ChatActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.d("Cloudinary Quickstart", "Upload failed");
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        }).dispatch();
    }

}