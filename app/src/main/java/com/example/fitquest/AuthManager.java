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
        // Check username uniqueness first
        usernameRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onFailure("Username already taken");
                } else {
                    // Username available, proceed with account creation
                    createAccountWithEmail(email, password, username, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Failed to check username: " + error.getMessage());
            }
        });
    }

    private void createAccountWithEmail(String email, String password, String username, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();

                        if (firebaseUser == null) {
                            callback.onFailure("User is null after sign-up");
                            return;
                        }

                        String uid = firebaseUser.getUid();
                        UserModel userModel = new UserModel(uid, username, email);

                        // Check if username already exists, now that user is authenticated
                        usernameRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Username already taken → generate a fallback username
                                    String uniqueUsername = username + "_" + uid.substring(0, 5);
                                    userModel.setUsername(uniqueUsername);

                                    // Save user data with fallback username
                                    saveUserData(uid, userModel, uniqueUsername, firebaseUser, callback);
                                } else {
                                    // Username available → proceed normally
                                    saveUserData(uid, userModel, username, firebaseUser, callback);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Even if username check fails, continue saving data with fallback username
                                String fallbackUsername = username + "_" + uid.substring(0, 5);
                                userModel.setUsername(fallbackUsername);
                                saveUserData(uid, userModel, fallbackUsername, firebaseUser, callback);
                            }
                        });

                    } else {
                        // Handle sign-up failure
                        String errorMessage = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown error occurred";

                        if (errorMessage != null && errorMessage.toLowerCase().contains("email")) {
                            callback.onFailure("An account with this email already exists. Try logging in instead.");
                        } else {
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    private void saveUserData(String uid, UserModel userModel, String username, FirebaseUser firebaseUser, AuthCallback callback) {
        dbRef.child(uid).setValue(userModel)
                .addOnSuccessListener(unused -> {
                    usernameRef.child(username).setValue(uid)
                            .addOnSuccessListener(unused2 -> callback.onSuccess(firebaseUser))
                            .addOnFailureListener(e -> {
                                Log.w("AuthManager", "Failed to map username: " + e.getMessage());
                                callback.onSuccess(firebaseUser); // Continue even if username mapping fails
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AuthManager", "Database save failed: " + e.getMessage());
                    callback.onFailure("Database error: " + e.getMessage());
                });
    }



    // ─────────────── OAuth login (Google/Facebook) ───────────────
    public void loginWithCredential(AuthCredential credential, AuthCallback callback) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            // Check if this is a new user or existing user
                            String uid = user.getUid();
                            String email = user.getEmail();
                            String displayName = user.getDisplayName();
                            
                            // Save/update user data in DB
                            UserModel userModel = new UserModel(uid, displayName != null ? displayName : "User", email);
                            dbRef.child(uid).setValue(userModel)
                                    .addOnSuccessListener(unused -> {
                                        // If user has a display name, also save it as username
                                        if (displayName != null && !displayName.isEmpty()) {
                                            // Check if username is available
                                            usernameRef.child(displayName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (!snapshot.exists()) {
                                                        // Username available, save it
                                                        usernameRef.child(displayName).setValue(uid);
                                                    }
                                                    callback.onSuccess(user);
                                                }
                                                
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    callback.onSuccess(user); // Continue even if username save fails
                                                }
                                            });
                                        } else {
                                            callback.onSuccess(user);
                                        }
                                    })
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
