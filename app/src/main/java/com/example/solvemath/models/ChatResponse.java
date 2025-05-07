package com.example.solvemath.models;

import com.google.gson.annotations.SerializedName;


public class ChatResponse {
    @SerializedName("result")
    private String result;

    @SerializedName("problem")
    private String problem;

    @SerializedName("summary")
    private String summary;

    @SerializedName("file_url")
    private String fileUrl;

    public String getSummary() {
        return summary;
    }

    public String getProblem() {
        return problem;
    }

    public String getResult() {
        return result;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}