package com.example.fitquest;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private final FirebaseAuth auth;
    private final DatabaseReference dbRef;
    private final DatabaseReference usernameRef;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");        // users/$uid
        usernameRef = FirebaseDatabase.getInstance().getReference("usernames"); // usernames/$username
    }

    // ─────────────── Getters ───────────────
    public FirebaseAuth getFirebaseAuth() {
        return auth;
    }

    public DatabaseReference getDbRef() {
        return dbRef;
    }

    public DatabaseReference getUsernameRef() {
        return usernameRef;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // ─────────────── Email/Password login ───────────────
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        callback.onSuccess(task.getResult().getUser());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    // ─────────────── Email/Password account creation ───────────────
    public void createAccount(String email, String password, String username, AuthCallback callback) {
        // Check username uniqueness
        usernameRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onFailure("Username already taken");
                } else {
                    // Username available, create account
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    FirebaseUser firebaseUser = task.getResult().getUser();
                                    if (firebaseUser != null) {
                                        String uid = firebaseUser.getUid();

                                        // Save user data
                                        UserModel userModel = new UserModel(uid, username, email);
                                        dbRef.child(uid).setValue(userModel)
                                                .addOnSuccessListener(unused -> {
                                                    // Map username to uid
                                                    usernameRef.child(username).setValue(uid);
                                                    callback.onSuccess(firebaseUser);
                                                })
                                                .addOnFailureListener(e -> callback.onFailure("Database error: " + e.getMessage()));
                                    } else {
                                        callback.onFailure("User is null after sign up");
                                    }
                                } else {
                                    callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error occurred");
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Failed to check username: " + error.getMessage());
            }
        });
    }

    // ─────────────── OAuth login (Google/Facebook) ───────────────
    public void loginWithCredential(AuthCredential credential, AuthCallback callback) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            // Save user in DB if first login
                            dbRef.child(user.getUid()).child("email").setValue(user.getEmail())
                                    .addOnSuccessListener(unused -> callback.onSuccess(user))
                                    .addOnFailureListener(e -> callback.onFailure("Database save failed: " + e.getMessage()));
                        } else {
                            callback.onFailure("User is null after OAuth login");
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "OAuth login failed");
                    }
                });
    }

    // ─────────────── Logout ───────────────
    public void logout() {
        auth.signOut();
    }

    // ─────────────── Callback interface ───────────────
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }
}
