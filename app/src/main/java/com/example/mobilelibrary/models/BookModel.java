package com.example.mobilelibrary.models;

public class BookModel {
    public String userId;
    public String title;
    public String author;
    public int rating;

    public BookModel() {
    }

    public BookModel(String userId, String title, String author, int rating) {
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
