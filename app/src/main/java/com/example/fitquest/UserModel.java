package com.example.fitquest;

public class UserModel {
    public String uid;
    public String username;
    public String email;

    // Required for Firebase
    public UserModel() {}

    public UserModel(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
