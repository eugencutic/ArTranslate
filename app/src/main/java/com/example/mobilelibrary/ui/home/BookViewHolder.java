package com.example.mobilelibrary.ui.home;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilelibrary.R;
import com.example.mobilelibrary.models.BookModel;

public class BookViewHolder extends RecyclerView.ViewHolder {

    private BookModel model;
    private TextView textViewBookTitle;
    private TextView textViewBookAuthor;

    public BookModel getModel() {
        return model;
    }

    public void setModel(BookModel model) {
        this.model = model;
    }

    public TextView getTextViewBookTitle() {
        return textViewBookTitle;
    }

    public void setTextViewBookTitle(TextView textViewBookTitle) {
        this.textViewBookTitle = textViewBookTitle;
    }

    public TextView getTextViewBookAuthor() {
        return textViewBookAuthor;
    }

    public void setTextViewBookAuthor(TextView textViewBookAuthor) {
        this.textViewBookAuthor = textViewBookAuthor;
    }

    public BookViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewBookAuthor = itemView.findViewById(R.id.text_view_book_author);
        textViewBookTitle = itemView.findViewById(R.id.text_view_book_title);

        // TODO: set on click listener for long press -> details page
    }
}
