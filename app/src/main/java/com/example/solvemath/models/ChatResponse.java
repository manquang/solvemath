package com.example.solvemath.models;

import com.google.gson.annotations.SerializedName;


public class ChatResponse {
    @SerializedName("result")
    private String result;

    @SerializedName("public_id")
    private String publicID;

    @SerializedName("summary")
    private String summary;

    @SerializedName("file_url")
    private String fileUrl;

    public String getSummary() {
        return summary;
    }

    public String getResult() {
        return result;
    }

    public String getFileUrl() {
        return fileUrl;
    }
    public String getPublicID() {
        return publicID;
    }
}