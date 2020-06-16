package com.example.mobilelibrary.models;

public class TranslatedText {
    private String userId;
    private String textToTranslate;
    private String translatedText;

    public TranslatedText() {

    }

    public TranslatedText(String userId, String textToTranslate, String translatedText) {
        this.userId = userId;
        this.textToTranslate = textToTranslate;
        this.translatedText = translatedText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTextToTranslate() {
        return textToTranslate;
    }

    public void setTextToTranslate(String textToTranslate) {
        this.textToTranslate = textToTranslate;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
