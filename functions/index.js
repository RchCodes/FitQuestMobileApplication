const functions = require("firebase-functions");
const admin = require("firebase-admin");
const pvp = require("./pvp");

admin.initializeApp();
const db = admin.firestore();

// Keep Firestore clean (ignores undefined props)
db.settings({ ignoreUndefinedProperties: true });

// Combat validation constants
const MATCH_EXPIRY_HOURS = 24;
const MAX_COMBAT_TURNS = 30;
const MIN_COMBAT_TURNS = 1;

// Export PvP functions
exports.createPvPMatch = pvp.createPvPMatch;
exports.submitPvPResult = pvp.submitPvPResult;

/**
 * Cloud Function: submitExercise
 * Called when a user submits an exercise.
 * Updates XP and logs activity in Firestore.
 */
exports.submitExercise = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const uid = context.auth.uid;
  const xpEarned = data.xp || 10;

  try {
    const userRef = db.collection("users").doc(uid);

    await userRef.set(
      {
        xp: admin.firestore.FieldValue.increment(xpEarned),
        lastActive: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    await userRef.collection("exerciseLogs").add({
      exercise: data.exercise || "Unknown",
      reps: data.reps || 0,
      xpEarned: xpEarned,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { success: true, xpGained: xpEarned };
  } catch (error) {
    console.error("Error in submitExercise:", error);
    throw new functions.https.HttpsError(
      "internal",
      "Error processing exercise submission"
    );
  }
});

/**
 * Cloud Function: createPvPMatch
 * Creates a PvP match document in Firestore.
 */
exports.createPvPMatch = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const uid = context.auth.uid;

  try {
    const matchRef = await db.collection("pvpMatches").add({
      players: [uid, data.opponentId],
      status: "pending",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { success: true, matchId: matchRef.id };
  } catch (error) {
    console.error("Error in createPvPMatch:", error);
    throw new functions.https.HttpsError(
      "internal",
      "Error creating PvP match"
    );
  }
});

