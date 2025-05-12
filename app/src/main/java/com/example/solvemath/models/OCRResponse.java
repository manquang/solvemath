package com.example.solvemath.models;

import com.google.gson.annotations.SerializedName;

public class OCRResponse {
    @SerializedName("ocr_text")
    private String ocrText;

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }
}
