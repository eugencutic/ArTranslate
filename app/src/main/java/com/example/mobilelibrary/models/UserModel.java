package com.example.mobilelibrary.models;

import java.util.List;

public class UserModel {
    public String userId;
    public String email;
    public List<UserModel> friends;

    public UserModel() {
    }

    public UserModel(String userId, String email, List<UserModel> friends) {
        this.userId = userId;
        this.email = email;
        this.friends = friends;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UserModel> getFriends() {
        return friends;
    }

    public void setFriends(List<UserModel> friends) {
        this.friends = friends;
    }
}
