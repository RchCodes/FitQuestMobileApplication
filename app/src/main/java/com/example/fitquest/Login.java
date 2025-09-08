package com.example.fitquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private Button buttonLogin, buttonSignup;
    private EditText editUsername, editPassword;
    private TextView textForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Input fields
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);

        // Buttons
        buttonLogin = findViewById(R.id.button_login);
        buttonSignup = findViewById(R.id.button_signup);

        // Forgot password text
        textForgotPassword = findViewById(R.id.text_forgot_password);

        // LOGIN button → check saved account
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retrieve saved credentials
                SharedPreferences sharedPreferences = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE);
                String savedUser = sharedPreferences.getString("username", "");
                String savedPass = sharedPreferences.getString("password", "");

                if (username.equals(savedUser) && password.equals(savedPass)) {
                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, AvatarCreationActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // SIGN UP button → AccountCreation
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, AccountCreation.class);
                startActivity(intent);
            }
        });

        // Forgot Password → show saved password (for testing only)
        textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE);
                String savedUser = sharedPreferences.getString("username", "");
                String savedPass = sharedPreferences.getString("password", "");

                if (!savedUser.isEmpty() && !savedPass.isEmpty()) {
                    Toast.makeText(Login.this, "Password for " + savedUser + ": " + savedPass, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Login.this, "No account found. Please sign up first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
