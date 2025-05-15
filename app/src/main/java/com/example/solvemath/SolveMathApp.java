package com.example.solvemath;

import android.app.Application;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SolveMathApp extends Application {
    private static Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();


        //Retrofit
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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