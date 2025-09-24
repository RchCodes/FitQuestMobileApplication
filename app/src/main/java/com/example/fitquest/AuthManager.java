package com.example.fitquest;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private final FirebaseAuth auth;
    private final DatabaseReference dbRef;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance()
                .getReference("users"); // Realtime DB users node
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    // 🔹 Create account and store user in RTDB
    public void createAccount(String email, String password, String username, Context context, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();

                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            UserModel userModel = new UserModel(uid, username, email);

                            dbRef.child(uid).setValue(userModel)
                                    .addOnSuccessListener(unused -> {
                                        Log.d(TAG, "User saved in RTDB");
                                        callback.onSuccess(firebaseUser);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Database save failed", e);
                                        callback.onFailure("Database error: " + e.getMessage());
                                    });
                        } else {
                            callback.onFailure("User is null after sign up");
                        }
                    } else {
                        if (task.getException() != null) {
                            callback.onFailure(task.getException().getMessage());
                        } else {
                            callback.onFailure("Unknown error occurred");
                        }
                    }
                });
    }

    // 🔹 Login with email/password
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        callback.onSuccess(firebaseUser);
                    } else {
                        if (task.getException() != null) {
                            callback.onFailure(task.getException().getMessage());
                        } else {
                            callback.onFailure("Login failed");
                        }
                    }
                });
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}
