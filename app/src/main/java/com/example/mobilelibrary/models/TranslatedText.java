package com.example.mobilelibrary.models;

public class TranslatedText {
    private String userId;
    private String textToTranslate;
    private String translatedText;
    private String translatedToLanguage;
    private String translatedFromLanguage;

    public TranslatedText() {

    }

    public TranslatedText(String userId, String textToTranslate, String translatedText, String translatedFromLanguage, String translatedToLanguage) {
        this.userId = userId;
        this.textToTranslate = textToTranslate;
        this.translatedText = translatedText;
        this.translatedFromLanguage = translatedFromLanguage;
        this.translatedToLanguage = translatedToLanguage;
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

    public String getTranslatedToLanguage() {
        return translatedToLanguage;
    }

    public void setTranslatedToLanguage(String translatedToLanguage) {
        this.translatedToLanguage = translatedToLanguage;
    }

    public String getTranslatedFromLanguage() {
        return translatedFromLanguage;
    }

    public void setTranslatedFromLanguage(String translatedFromLanguage) {
        this.translatedFromLanguage = translatedFromLanguage;
    }
}
