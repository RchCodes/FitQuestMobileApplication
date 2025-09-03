package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AccountCreation extends AppCompatActivity {

    private EditText etEmail, etUser, etPassword, etConfirmPassword;
    private ImageView btnSignUp, btnFacebook, btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_creation);

        // Initialize input fields
        etEmail = findViewById(R.id.etEmail);
        etUser = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Initialize buttons
        btnSignUp = findViewById(R.id.btnSignUp);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Handle Sign Up button click
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String username = etUser.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(AccountCreation.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(AccountCreation.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Account creation successful
                    Toast.makeText(AccountCreation.this, "Account created: " + username, Toast.LENGTH_SHORT).show();

                    // Redirect to AvatarCreationActivity
                    Intent intent = new Intent(AccountCreation.this, AvatarCreationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Handle Facebook button (future implementation)
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountCreation.this, "Facebook Sign Up coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Google button (future implementation)
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountCreation.this, "Google Sign Up coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
