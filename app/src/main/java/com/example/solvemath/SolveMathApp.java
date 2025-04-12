package com.example.solvemath;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

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
        config.put("api_key", BuildConfig.API_KEY);
        config.put("api_secret", BuildConfig.API_SECRET);
        MediaManager.init(this, config);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getRetrofitInstance() {
        return retrofit;
    }
}