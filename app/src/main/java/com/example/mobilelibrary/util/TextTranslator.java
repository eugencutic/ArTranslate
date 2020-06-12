package com.example.mobilelibrary.util;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class TextTranslator {

    private String translatedText;
    private final String TAG = "TRANSLATE";

    private FirebaseTranslator getFirebaseTranslator() {
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.RO)
                .build();
        return FirebaseNaturalLanguage.getInstance().getTranslator(options);
    }

    public void translateText(String textToTranslate) {

        FirebaseTranslator translator = getFirebaseTranslator();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
            .requireWifi()
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
                        Log.e(TAG, "Exceptionn while downloading model", e);
                    }
                });
    }

    private void handleTranslate(FirebaseTranslator translator, String textToTranslate) {
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
                        Log.e(TAG, "Exceptionn while translating", e);
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
