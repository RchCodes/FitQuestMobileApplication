package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class Login extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private ImageView btnGoogle, btnFacebook;
    private FrameLayout progressOverlay;
    private AuthManager authManager;
    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        etEmail = findViewById(R.id.edit_username);
        etPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.button_login);
        btnSignup = findViewById(R.id.button_signup);
        btnGoogle = findViewById(R.id.google_icon);
        btnFacebook = findViewById(R.id.fb_icon);
        progressOverlay = findViewById(R.id.progressOverlay);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        authManager = new AuthManager();

        setupGoogleSignIn();
        setupFacebookSignIn();

        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnSignup.setOnClickListener(v -> startActivity(new Intent(Login.this, AccountCreation.class)));
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnFacebook.setOnClickListener(v -> LoginManager.getInstance().logInWithReadPermissions(Login.this,
                Arrays.asList("email", "public_profile")));

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                etEmail.requestFocus();
                return;
            }

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean emailPasswordUser = task.getResult().getSignInMethods().contains("password");
                            if (emailPasswordUser) {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                        .addOnCompleteListener(resetTask -> {
                                            if (resetTask.isSuccessful()) {
                                                Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(this, "Error: " + resetTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(this, "This email is linked with Google/Facebook login. Reset your password there.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }

    // ─────────────── Email Login ───────────────
    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        progressOverlay.setVisibility(View.VISIBLE);

        // Check which providers are linked to this email
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (task.getResult() == null || task.getResult().getSignInMethods().isEmpty()) {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "No account found with this email", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!task.getResult().getSignInMethods().contains("password")) {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "This email is linked with Google/Facebook. Please login with the respective provider.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Email uses password, proceed with login
                    authManager.login(email, password, new AuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            progressOverlay.setVisibility(View.GONE);
                            UserUtils.checkAvatarAndProceed(user, Login.this);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            progressOverlay.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                });
    }



    // ─────────────── Google Login ───────────────
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        loginWithCredential(credential);
    }

    // ─────────────── Facebook Login ───────────────
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
                Toast.makeText(Login.this, "Facebook login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Login.this, "Facebook login failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─────────────── Unified OAuth Login ───────────────
    private void loginWithCredential(AuthCredential credential) {
        progressOverlay.setVisibility(View.VISIBLE);
        authManager.loginWithCredential(credential, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressOverlay.setVisibility(View.GONE);
                UserUtils.checkAvatarAndProceed(user, Login.this);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressOverlay.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

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
