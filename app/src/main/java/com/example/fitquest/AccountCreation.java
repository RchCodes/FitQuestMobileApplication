package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AccountCreation extends AppCompatActivity {

    private EditText editUsername;
    private ImageView btnConfirm; // Changed from Button to ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_creation);

        editUsername = findViewById(R.id.edit_username);
        btnConfirm = findViewById(R.id.btn_confirm); // ImageView ID from XML

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editUsername.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(AccountCreation.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                } else {
                    // Save username (could be SharedPreferences, Database, etc.)
                    Toast.makeText(AccountCreation.this, "Account created: " + username, Toast.LENGTH_SHORT).show();

                    // Example: Go to Main Activity
                    Intent intent = new Intent(AccountCreation.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
