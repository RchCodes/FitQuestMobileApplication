package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnSignup;
    private ImageView btnFacebook, btnGoogle;
    private FrameLayout progressOverlay;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // 🔹 Match with new XML IDs
        etUsername = findViewById(R.id.edit_username);
        etPassword = findViewById(R.id.edit_password);

        btnLogin = findViewById(R.id.button_login);
        btnSignup = findViewById(R.id.button_signup);
        btnFacebook = findViewById(R.id.fb_icon);
        btnGoogle = findViewById(R.id.google_icon);
        progressOverlay = findViewById(R.id.progressOverlay);

        authManager = new AuthManager();

        // 🔹 Login button
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if username looks like an email
            if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            progressOverlay.setVisibility(View.VISIBLE);

            // Firebase login
            authManager.login(username, password, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Go to main app screen
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        // 🔹 Signup button
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(Login.this, AccountCreation.class))
        );

        // 🔹 Placeholder for social login
        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Facebook Login coming soon!", Toast.LENGTH_SHORT).show()
        );

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        );
    }
}
