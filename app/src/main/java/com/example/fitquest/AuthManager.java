package com.example.fitquest;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    // ✅ Create new account
    public void createAccount(String email, String password, String username, Activity activity, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Save extra user info in database
                            UserModel newUser = new UserModel(user.getUid(), username, email);
                            mDatabase.child(user.getUid()).setValue(newUser);

                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("User creation failed: null user.");
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    // ✅ Sign in existing account
    public void login(String email, String password, Activity activity, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    // ✅ Get currently logged-in user
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // ✅ Sign out
    public void logout() {
        mAuth.signOut();
    }
}
