package com.example.solvemath;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getRetrofitInstance() {
        return retrofit;
    }
}