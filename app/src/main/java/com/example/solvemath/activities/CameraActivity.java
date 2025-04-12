package com.example.solvemath.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.canhub.cropper.CropImageContract;


import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.cloudinary.android.MediaManager;
import com.example.solvemath.R;
import com.example.solvemath.databinding.ActivityCameraBinding;


import com.bumptech.glide.Glide;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.solvemath.utilities.Helper;
import com.google.common.util.concurrent.ListenableFuture;


import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    public static final int PERMISSION_CAMERA = 1;
    private ActivityCameraBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    startCrop(imageUri);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        EdgeToEdge.enable(this);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        //Permission
        checkPermissions();
//        //Start camera
        setupUI();
        startCamera(cameraFacing);

    }

    public void setupUI() {
        Window window = getWindow();
        WindowCompat.getInsetsController(window, window.getDecorView())
                .setAppearanceLightStatusBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.cameraActions, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        binding.back.setOnClickListener(v -> finish());
        binding.flipCamera.setOnClickListener(v -> {
            cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            startCamera(cameraFacing);
        });
    }


    public void startCamera(int cameraFacing) {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(binding.cameraPreview.getDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

                binding.capture.setOnClickListener(v -> takePicture(imageCapture));
                binding.flash.setOnClickListener(v -> setFlashIcon(camera));
                binding.gallery.setOnClickListener(view -> {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");
                    galleryLauncher.launch(pickIntent);
                });

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(CameraActivity.this, "Camera init failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

    }

    public void takePicture(ImageCapture imageCapture) {
        imageCapture.takePicture(Executors.newCachedThreadPool(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Bitmap bitmap = Helper.toBitmap(image);
                image.close(); // Giải phóng tài nguyên
                runOnUiThread(() -> startCrop(Helper.getTempImageUri(CameraActivity.this, bitmap)));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Capture failed", Toast.LENGTH_SHORT).show());

            }
        });
    }

    public void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            boolean isFlashOn = camera.getCameraInfo().getTorchState().getValue() == 1;
            camera.getCameraControl().enableTorch(!isFlashOn);
            binding.flash.setImageResource(isFlashOn ? R.drawable.round_flash_on_24 : R.drawable.round_flash_off_24);
        } else {
            Toast.makeText(CameraActivity.this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }


    public void startCrop(Uri imageUri) {
        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra("imageUri", imageUri);
        startActivity(intent);
    }


    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}
                    , PERMISSION_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSION_CAMERA:
                    Toast.makeText(this, "Camera access granted", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }
}