package com.example.mobilelibrary.ui.history;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilelibrary.R;
import com.example.mobilelibrary.models.TranslatedText;

public class TranslatedTextViewHolder extends RecyclerView.ViewHolder {

    private TranslatedText model;
    private TextView textViewTextToTranslate;
    private TextView textViewTranslatedText;
    private TextView textViewTranslatedToLanguage;
    private TextView textViewTranslatedFromLanguage;

    public TranslatedText getModel() { return  model; }

    public void setModel(TranslatedText model) {
        this.model = model;
    }

    public TextView getTextViewTextToTranslate() {
        return textViewTextToTranslate;
    }

    public void setTextViewTextToTranslate(TextView textViewTextToTranslate) {
        this.textViewTextToTranslate = textViewTextToTranslate;
    }

    public TextView getTextViewTranslatedText() {
        return textViewTranslatedText;
    }

    public void setTextViewTranslatedText(TextView textViewTranslatedText) {
        this.textViewTranslatedText = textViewTranslatedText;
    }

    public TextView getTextViewTranslatedFromLanguage() {
        return textViewTranslatedFromLanguage;
    }

    public void setTextViewTranslatedFromLanguage(TextView textViewTranslatedFromLanguage) {
        this.textViewTranslatedFromLanguage = textViewTranslatedFromLanguage;
    }

    public TextView getTextViewTranslatedToLanguage() {
        return textViewTranslatedToLanguage;
    }

    public void setTextViewTranslatedToLanguage(TextView textViewTranslatedToLanguage) {
        this.textViewTranslatedToLanguage = textViewTranslatedToLanguage;
    }

    public TranslatedTextViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewTextToTranslate = itemView.findViewById(R.id.text_view_text_to_translate);
        textViewTranslatedText = itemView.findViewById(R.id.text_view_translated_text);
        textViewTranslatedFromLanguage = itemView.findViewById(R.id.text_view_translated_from_language);
        textViewTranslatedToLanguage = itemView.findViewById(R.id.text_view_translated_to_language);

        // TODO: set on click listener for long press -> details page
    }
}
