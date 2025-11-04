package com.example.fitquest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Profile {

    private final Context context;
    private final ActivityResultLauncher<Intent> googleLauncher;
    private final Dialog dialog;
    private final AuthManager authManager;
    private AvatarModel avatar;


    private TextView usernameView;
    private TextView classView;
    private TextView levelView;
    private ProgressBar expBar;
    private TextView progressText;
    private TextView playerIdView;
    private ImageView rankIconView;
    private TextView rankNameView;
    private ViewGroup badgesContainer;


    private final CallbackManager facebookCallbackManager = CallbackManager.Factory.create();

    public Profile(Context context, ActivityResultLauncher<Intent> googleLauncher) {
        this.context = context;
        this.googleLauncher = googleLauncher;
        this.authManager = new AuthManager();


        // Inflate layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.profile, null);
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Bind all views first
        bindViewsAndSetup(popupView);

        // Load avatar
        avatar = AvatarManager.loadAvatarOffline(context);
        if (avatar == null) avatar = new AvatarModel();

        // Now populate profile data safely
        populateProfileData();

        // Set profile change listener
        avatar.setProfileChangeListener(updatedAvatar -> {
            avatar = updatedAvatar;
            populateProfileData();
        });

        setupFacebookCallback();

    }

    private void bindViewsAndSetup(View popupView) {
        usernameView = popupView.findViewById(R.id.username);
        classView = popupView.findViewById(R.id.classView);
        levelView = popupView.findViewById(R.id.level);
        expBar = popupView.findViewById(R.id.exp_bar);
        progressText = popupView.findViewById(R.id.progressText);
        playerIdView = popupView.findViewById(R.id.player_id);
        rankIconView = popupView.findViewById(R.id.rank_icon);
        rankNameView = popupView.findViewById(R.id.rank_name);
    badgesContainer = popupView.findViewById(R.id.badges_container);
        Button bindButton = popupView.findViewById(R.id.bind_button);
        bindButton.setOnClickListener(v -> showBindOptions());
        Button switchButton = popupView.findViewById(R.id.switch_button);
        switchButton.setOnClickListener(v -> switchAccount());
        ImageButton closeButton = popupView.findViewById(R.id.btn_close_profile);
        closeButton.setOnClickListener(v -> dismiss());
    }

    private void populateProfileData() {
        if (avatar == null) return;

        // Username
        usernameView.setText(avatar.getUsername());

        // Class
        classView.setText(avatar.getPlayerClass());

        // Level
        levelView.setText("LV. " + avatar.getLevel());

        // EXP Bar
        int currentXp = avatar.getXp();
        int currentLevel = avatar.getLevel();
        int maxLevel = LevelProgression.getMaxLevel();
        
        if (currentLevel >= maxLevel) {
            // User is at max level
            expBar.setMax(100);
            expBar.setProgress(100);
            progressText.setText("MAX LEVEL");
        } else {
            int xpForNextLevel = LevelProgression.getMaxXpForLevel(currentLevel);
            expBar.setMax(xpForNextLevel);
            expBar.setProgress(currentXp);
            progressText.setText(currentXp + "/" + xpForNextLevel);
        }

        // Player ID
        playerIdView.setText("ID: " + avatar.getPlayerId());

        // Rank icon and name
        rankIconView.setImageResource(avatar.getRankDrawableRes());
        rankNameView.setText(avatar.getRankName());

        // Badges: show up to 5 most recent
        if (badgesContainer != null) {
            badgesContainer.removeAllViews();

            List<Integer> badges = (avatar != null) ? avatar.getAvatarBadges() : new ArrayList<>();
            
            // Debug logging
            android.util.Log.d("Profile", "Avatar badges count: " + badges.size());
            for (int i = 0; i < badges.size(); i++) {
                android.util.Log.d("Profile", "Badge " + i + ": " + badges.get(i));
            }
            
            if (!badges.isEmpty()) {
                int count = badges.size();
                int start = Math.max(0, count - 5); // show last 5 badges
                for (int i = count - 1; i >= start; i--) {
                    Integer badgeRes = badges.get(i);
                    if (badgeRes != null && badgeRes != 0) {
                        ImageView badgeView = new ImageView(context);

                        int size = (int) (48 * context.getResources().getDisplayMetrics().density);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                        params.setMargins(4, 4, 4, 4); // optional spacing
                        badgeView.setLayoutParams(params);

                        badgeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        try {
                            badgeView.setImageResource(badgeRes);
                            badgesContainer.addView(badgeView);
                            android.util.Log.d("Profile", "Added badge: " + badgeRes);
                        } catch (Exception e) {
                            android.util.Log.e("Profile", "Failed to load badge resource: " + badgeRes, e);
                        }
                    }
                }
            } else {
                // Show placeholder when no badges
                TextView noBadgesText = new TextView(context);
                noBadgesText.setText("No badges yet");
                noBadgesText.setTextColor(android.graphics.Color.GRAY);
                noBadgesText.setTextSize(12);
                noBadgesText.setGravity(android.view.Gravity.CENTER);
                badgesContainer.addView(noBadgesText);
            }
        }

    }

    public void refreshProfile() {
        populateProfileData();
    }

    private void switchAccount() {
        new AlertDialog.Builder(context)
                .setTitle("Switch Account")
                .setMessage("Do you want to sign out and switch accounts?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Firebase sign-out
                    FirebaseAuth.getInstance().signOut();

                    // Facebook sign-out
                    LoginManager.getInstance().logOut();

                    // Google sign-out
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .build();
                    GoogleSignInClient googleClient = GoogleSignIn.getClient(context, gso);
                    googleClient.signOut().addOnCompleteListener(task -> {
                        Toast.makeText(context, "Signed out. Please log in again.", Toast.LENGTH_SHORT).show();

                        // Redirect to Login screen
                        Intent intent = new Intent(context, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void showBindOptions() {
        String[] options = {"Bind Google", "Bind Facebook"};
        new AlertDialog.Builder(context)
                .setTitle("Bind Account")
                .setItems(options, (d, which) -> {
                    if (which == 0) bindGoogle();
                    else bindFacebook();
                }).show();
    }

    /** Google account linking */
    private void bindGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(context, gso);
        googleLauncher.launch(client.getSignInIntent());
    }

    /** Call this from MainActivity after successful Google Sign-In */
    public void handleGoogleLink(GoogleSignInAccount account) {
        if (account == null) {
            Toast.makeText(context, "Google account not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = authManager.getFirebaseAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Google account linked!", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(context, "Link failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Facebook account linking */
    private void bindFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
                (Activity) context,
                Arrays.asList("email", "public_profile")
        );
    }

    /** Setup Facebook login callback */
    private void setupFacebookCallback() {
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                linkFacebookWithFirebase(result.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(context, "Facebook login canceled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(context, "Facebook login failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Link Facebook token to Firebase user */
    private void linkFacebookWithFirebase(AccessToken token) {
        FirebaseUser currentUser = authManager.getFirebaseAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Facebook account linked!", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(context, "Link failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Expose callback manager for MainActivity to forward onActivityResult */
    public CallbackManager getFacebookCallbackManager() {
        return facebookCallbackManager;
    }

    /** Show profile dialog */
    public void show() {
        dialog.show();
    }

    /** Dismiss profile dialog */
    public void dismiss() {
        dialog.dismiss();
    }
}
