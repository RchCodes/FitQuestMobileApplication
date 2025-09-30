package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class UserUtils {

    /**
     * Checks if the user has an avatar set in Realtime Database.
     * Redirects to MainActivity if avatar exists,
     * or AvatarCreationActivity if avatar is missing.
     */
    public static void checkAvatarAndProceed(FirebaseUser user, Context context) {
        if (user == null) return;

        AuthManager authManager = new AuthManager();
        authManager.getDbRef()
                .child(user.getUid())
                .child("avatar")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Intent intent;
                        if (snapshot.exists()) {
                            intent = new Intent(context, MainActivity.class);
                        } else {
                            intent = new Intent(context, AvatarCreationActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to check avatar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
