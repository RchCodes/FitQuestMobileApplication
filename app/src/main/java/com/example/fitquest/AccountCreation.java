package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AccountCreation extends AppCompatActivity {

    private EditText etEmail, etUser, etPassword, etConfirmPassword;
    private ImageView btnSignUp, btnFacebook, btnGoogle;
    private FrameLayout progressOverlay;

    private static final String TAG = "AccountCreation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_creation);

        etEmail = findViewById(R.id.etEmail);
        etUser = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnGoogle = findViewById(R.id.btnGoogle);
        progressOverlay = findViewById(R.id.progressOverlay);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUser.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // 🔹 Validation
            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 8 || !password.matches(".*[A-Z].*") ||
                    !password.matches(".*[a-z].*") || !password.matches(".*\\d.*") ||
                    !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                Toast.makeText(this, "Password must contain upper, lower, number & special char", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            progressOverlay.setVisibility(View.VISIBLE);

            // 🔹 Check username uniqueness in RTDB
            FirebaseDatabase.getInstance().getReference("users")
                    .orderByChild("username").equalTo(username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            progressOverlay.setVisibility(View.GONE);
                            Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed to create account
                            AuthManager authManager = new AuthManager();
                            authManager.createAccount(email, password, username, this, new AuthManager.AuthCallback() {
                                @Override
                                public void onSuccess(FirebaseUser user) {
                                    progressOverlay.setVisibility(View.GONE);
                                    Toast.makeText(AccountCreation.this, "Account created!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AccountCreation.this, AvatarCreationActivity.class));
                                    finish();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    progressOverlay.setVisibility(View.GONE);
                                    Log.e(TAG, "Sign up failed: " + errorMessage);

                                    if (errorMessage.contains("email")) {
                                        Toast.makeText(AccountCreation.this, "Invalid or already used email", Toast.LENGTH_LONG).show();
                                    } else if (errorMessage.contains("Database")) {
                                        Toast.makeText(AccountCreation.this, "Database error: " + errorMessage, Toast.LENGTH_LONG).show();
                                    } else if (errorMessage.contains("network")) {
                                        Toast.makeText(AccountCreation.this, "Network error, please check your connection", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(AccountCreation.this, "Sign up failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressOverlay.setVisibility(View.GONE);
                        Log.e(TAG, "Username check failed", e);
                        Toast.makeText(this, "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Facebook Sign Up coming soon!", Toast.LENGTH_SHORT).show()
        );

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign Up coming soon!", Toast.LENGTH_SHORT).show()
        );
    }
}
