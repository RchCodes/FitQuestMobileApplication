package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserUtils {

    public static void checkAvatarAndProceed(FirebaseUser user, Context context) {
        if (user == null) {
            Log.w("UserUtils", "checkAvatarAndProceed: user is null");
            return;
        }

        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        Log.d("UserUtils", "Checking avatar at: " + userRef.toString());

        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d("UserUtils", "user snapshot exists=" + snapshot.exists() + ", children=" + snapshot.getChildrenCount());
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Log.d("UserUtils", " child: " + child.getKey() + " -> " + (child.getValue() == null ? "null" : child.getValue().toString()));
                    }

                    boolean hasAvatarNode = snapshot.hasChild("avatar");
                    boolean hasFlatOutfit = snapshot.hasChild("outfit") || snapshot.hasChild("avatar/outfit");
                    boolean hasAvatarBody = false;

                    // If avatar was saved as "avatar/body" via updateChildren, the snapshot
                    // won't directly have a child named "avatar/body" but snapshot.child("avatar").exists()
                    if (snapshot.child("avatar").exists()) {
                        hasAvatarBody = snapshot.child("avatar").hasChild("body") || snapshot.child("avatar").getChildrenCount() > 0;
                    } else {
                        // fallback: check some likely flat keys
                        hasAvatarBody = snapshot.hasChild("avatar/body") || snapshot.hasChild("body");
                    }

                    boolean avatarExists = hasAvatarNode || hasFlatOutfit || hasAvatarBody;

                    if (avatarExists) {
                        // best: load the full avatar model from online and start MainActivity in callback
                        AvatarManager.loadAvatarOnline(new AvatarManager.AvatarLoadCallback() {
                            @Override
                            public void onLoaded(AvatarModel avatar) {
                                // optional: save offline copy
                                AvatarManager.saveAvatarOffline(context, avatar);

                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onError(String message) {
                                Log.w("UserUtils", "Avatar exists but failed to load: " + message);
                                // Fallback: still proceed to MainActivity (or handle per your UX)
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        });
                    } else {
                        // No avatar, go to creation
                        Intent intent = new Intent(context, AvatarCreationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e("UserUtils", "checkAvatarAndProceed error", e);
                    // Safe fallback
                    Intent intent = new Intent(context, AvatarCreationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserUtils", "Failed DB check: " + error.getMessage(), error.toException());
                Toast.makeText(context, "Failed to check avatar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                // Safe fallback
                Intent intent = new Intent(context, AvatarCreationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
    }
}

