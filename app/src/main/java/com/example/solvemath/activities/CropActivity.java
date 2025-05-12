package com.example.solvemath.activities;


import static com.example.solvemath.SolveMathApp.getRetrofitInstance;

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
import com.example.solvemath.ApiService;
import com.example.solvemath.databinding.ActivityCropBinding;
import com.example.solvemath.utilities.Helper;


import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropActivity extends AppCompatActivity {
    private ActivityCropBinding binding;
    private ApiService apiService;

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

        apiService = getRetrofitInstance().create(ApiService.class);

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

        File file = new File(uri.getPath());
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);

        apiService.uploadImage(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        String imageUrl = jsonObject.getString("img_url");
                        String publicImageId = jsonObject.getString("public_id");
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("img_url", imageUrl);
                        intent.putExtra("public_id", publicImageId);
                        Toast.makeText(getApplicationContext(), "Upload thành công", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Lỗi khi đọc phản hồi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Upload thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadError", t.getMessage(), t);
            }
        });
    }

}