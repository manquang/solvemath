package com.example.solvemath;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import com.example.solvemath.models.ChatRequest;
import com.example.solvemath.models.ChatResponse;
import com.example.solvemath.models.OCRResponse;

import java.util.List;
import java.util.Map;

public interface ApiService {

    @HTTP(method = "DELETE", path = "/delete", hasBody = true)
    Call<ResponseBody> deleteSession(@Body Map<String, List<String>> body);
    @Multipart
    @POST("/upload_image")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);
    @POST("/extract_ocr")
    Call<OCRResponse> extractOCR(@Body ChatRequest request);
    @POST("/qa")
    Call<ChatResponse> sendQA(@Body ChatRequest request);
}