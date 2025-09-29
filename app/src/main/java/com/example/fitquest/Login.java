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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;


public class Login extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnSignup;
    private ImageView btnFacebook, btnGoogle;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    private FrameLayout progressOverlay;

    private AuthManager authManager;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        callbackManager = CallbackManager.Factory.create();


        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        // 🔹 Match with new XML IDs
        etUsername = findViewById(R.id.edit_username);
        etPassword = findViewById(R.id.edit_password);

        btnLogin = findViewById(R.id.button_login);
        btnSignup = findViewById(R.id.button_signup);
        btnFacebook = findViewById(R.id.fb_icon);
        btnGoogle = findViewById(R.id.google_icon);
        progressOverlay = findViewById(R.id.progressOverlay);

        authManager = new AuthManager();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google button click
        btnGoogle.setOnClickListener(v -> signInWithGoogle());


        // 🔹 Login button
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            progressOverlay.setVisibility(View.VISIBLE);

            authManager.login(username, password, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    // Check if user has an avatar
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(user.getUid())
                            .child("avatar")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    progressOverlay.setVisibility(View.GONE);
                                    if (snapshot.exists()) {
                                        // Avatar exists → go to MainActivity
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                    } else {
                                        // No avatar → force creation
                                        startActivity(new Intent(Login.this, AvatarCreationActivity.class));
                                    }
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressOverlay.setVisibility(View.GONE);
                                    Toast.makeText(Login.this, "Failed to check avatar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
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
        btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    Login.this,
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
                            Toast.makeText(Login.this, "Facebook login canceled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            Toast.makeText(Login.this, "Facebook login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }

    private void handleFacebookAccessToken(com.facebook.AccessToken token) {
        progressOverlay.setVisibility(View.VISIBLE);

        AuthCredential credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token.getToken());
        authManager.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressOverlay.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = authManager.getFirebaseAuth().getCurrentUser();
                        if (user != null) {
                            // Save user in Realtime DB if first login
                            String uid = user.getUid();
                            authManager.getDbRef().child(uid).child("email").setValue(user.getEmail());

                            // Check avatar
                            checkAvatarAndProceed(user);
                        }
                    } else {
                        Toast.makeText(Login.this, "Facebook authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ────────────── Facebook Login ──────────────
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // ────────────── Google Login ──────────────
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn
                        .getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        progressOverlay.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        authManager.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressOverlay.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            // Save user in Realtime DB if first login
                            String uid = user.getUid();
                            authManager.getDbRef().child(uid).child("email").setValue(user.getEmail());

                            // Go to Avatar check logic
                            checkAvatarAndProceed(user);
                        }
                    } else {
                        Toast.makeText(Login.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkAvatarAndProceed(FirebaseUser user) {
        progressOverlay.setVisibility(View.VISIBLE);

        authManager.getDbRef()
                .child(user.getUid())
                .child("avatar")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        progressOverlay.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            // Avatar exists → go to MainActivity
                            startActivity(new Intent(Login.this, MainActivity.class));
                        } else {
                            // No avatar → force creation
                            startActivity(new Intent(Login.this, AvatarCreationActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Failed to check avatar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }




}
