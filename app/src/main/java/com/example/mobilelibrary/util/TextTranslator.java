package com.example.mobilelibrary.util;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TextTranslator {

    private String translatedText;
    private final String TAG = "TRANSLATE";

    private Translator getFirebaseTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.ROMANIAN)
                .build();
        return Translation.getClient(options);
    }

    public void translateText(String textToTranslate) {
        setTranslatedText(null);
        Translator translator = getFirebaseTranslator();

        DownloadConditions conditions = new DownloadConditions.Builder()
            .build();

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        handleTranslate(translator, textToTranslate);
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setTranslatedText("");
                        Log.e(TAG, "Exceptionn while downloading model", e);
                    }
                });
    }

    private void handleTranslate(Translator translator, String textToTranslate) {
        translator.translate(textToTranslate)
            .addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String translatedText) {
                        setTranslatedText(translatedText);
                        Log.i("INFO", translatedText);
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setTranslatedText("");
                        Log.e(TAG, "Exception while translating", e);
                    }
                });
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return this.translatedText;
    }
}
