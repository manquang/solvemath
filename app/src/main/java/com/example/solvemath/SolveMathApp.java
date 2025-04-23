package com.example.solvemath;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SolveMathApp extends Application {
    private static Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();

        // Cloudinary
        Map config = new HashMap();
        config.put("cloud_name", BuildConfig.CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUD_API_KEY);
        config.put("api_secret", BuildConfig.CLOUD_API_SECRET);
        MediaManager.init(this, config);

        //Retrofit
        String header = "Bearer " + BuildConfig.API_KEY;
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", header)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                }).connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getRetrofitInstance() {
        return retrofit;
    }
}