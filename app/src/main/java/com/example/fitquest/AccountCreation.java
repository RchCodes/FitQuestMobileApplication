package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Added import for Button
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;


public class AccountCreation extends AppCompatActivity {

    private EditText etEmail, etUser, etPassword, etConfirmPassword;
    private Button btnSignUp; // Changed from ImageView to Button
    private ImageView btnFacebook, btnGoogle;

    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;
    private AuthManager authManager;


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

        authManager = new AuthManager();

// Facebook
        callbackManager = CallbackManager.Factory.create();

// Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


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

        // Google Sign-Up
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

// Facebook Sign-Up
        btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    AccountCreation.this,
                    Arrays.asList("email", "public_profile")
            );

            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(AccountCreation.this, "Facebook login canceled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            Toast.makeText(AccountCreation.this, "Facebook login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        authManager.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authManager.getCurrentUser();
                        saveUserIfFirstTime(user);
                    } else {
                        Toast.makeText(AccountCreation.this, "Facebook auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        authManager.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authManager.getCurrentUser();
                        saveUserIfFirstTime(user);
                    } else {
                        Toast.makeText(AccountCreation.this, "Google auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserIfFirstTime(FirebaseUser user) {
        if (user == null) return;

        authManager.getDbRef().child(user.getUid()).child("email").setValue(user.getEmail())
                .addOnSuccessListener(unused -> {
                    // Redirect to Avatar Creation
                    startActivity(new Intent(AccountCreation.this, AvatarCreationActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AccountCreation.this, "DB save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // Google
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
