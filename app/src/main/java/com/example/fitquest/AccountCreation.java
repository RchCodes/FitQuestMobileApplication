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

import androidx.annotation.NonNull;
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

    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ImageView btnGoogle, btnFacebook;
    private FrameLayout progressOverlay;

    private AuthManager authManager;
    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_creation);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        progressOverlay = findViewById(R.id.progressOverlay);

        authManager = new AuthManager();

        setupGoogleSignIn();
        setupFacebookSignIn();
        MusicManager.start(this);

        btnSignUp.setOnClickListener(v -> signUpWithEmail());
        btnGoogle.setOnClickListener(v -> startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN));
        btnFacebook.setOnClickListener(v -> LoginManager.getInstance().logInWithReadPermissions(AccountCreation.this,
                Arrays.asList("email", "public_profile")));
    }

    // ─────────────── Email Sign-Up ───────────────
    private void signUpWithEmail() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() > 12) {
            Toast.makeText(this, "Username must be 12 characters or less", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be 8+ chars, include uppercase, lowercase, number & special char", Toast.LENGTH_LONG).show();
            return;
        }

        progressOverlay.setVisibility(View.VISIBLE);
        authManager.createAccount(email, password, username, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressOverlay.setVisibility(View.GONE);
                UserUtils.checkAvatarAndProceed(user, AccountCreation.this);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressOverlay.setVisibility(View.GONE);
                Toast.makeText(AccountCreation.this, "Sign up failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─────────────── Password Validation ───────────────
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(pattern);
    }

    // ─────────────── Google Sign-In ───────────────
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        loginWithCredential(credential);
    }

    // ─────────────── Facebook Sign-In ───────────────
    private void setupFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                loginWithCredential(credential);
            }

            @Override
            public void onCancel() {
                Toast.makeText(AccountCreation.this, "Facebook login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(AccountCreation.this, "Facebook login failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginWithCredential(AuthCredential credential) {
        progressOverlay.setVisibility(View.VISIBLE);
        authManager.loginWithCredential(credential, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressOverlay.setVisibility(View.GONE);
                UserUtils.checkAvatarAndProceed(user, AccountCreation.this);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressOverlay.setVisibility(View.GONE);
                Toast.makeText(AccountCreation.this, "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─────────────── Activity Result ───────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) handleGoogleSignIn(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
