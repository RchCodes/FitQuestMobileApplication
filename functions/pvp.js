/**
 * Cloud Function: createPvPMatch
 * Creates a new PvP match with a deterministic seed for combat simulation.
 */
exports.createPvPMatch = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const { opponentId } = data;
  if (!opponentId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing opponent ID"
    );
  }

  // Get player snapshots
  const [playerDoc, opponentDoc] = await Promise.all([
    db.collection("players").doc(context.auth.uid).get(),
    db.collection("players").doc(opponentId).get()
  ]);

  if (!playerDoc.exists || !opponentDoc.exists) {
    throw new functions.https.HttpsError(
      "not-found",
      "Player or opponent not found"
    );
  }

  const playerSnapshot = playerDoc.data().combatSnapshot;
  const opponentSnapshot = opponentDoc.data().combatSnapshot;

  // Generate deterministic seed from timestamp + UIDs
  const seed = Date.now() ^ 
    parseInt(context.auth.uid.slice(-8), 16) ^ 
    parseInt(opponentId.slice(-8), 16);

  // Create match document
  const matchRef = db.collection("pvp_matches").doc();
  await matchRef.set({
    player1Id: context.auth.uid,
    player2Id: opponentId,
    player1Snapshot: playerSnapshot,
    player2Snapshot: opponentSnapshot,
    seed: seed,
    status: "pending",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt: admin.firestore.Timestamp.fromMillis(
      Date.now() + (MATCH_EXPIRY_HOURS * 60 * 60 * 1000)
    )
  });

  return {
    matchId: matchRef.id,
    seed: seed
  };
});

/**
 * Cloud Function: submitPvPResult
 * Validates and records the result of a PvP match.
 */
exports.submitPvPResult = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const { matchId, result } = data;
  if (!matchId || !result) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing match ID or result"
    );
  }

  // Get match data
  const matchDoc = await db.collection("pvp_matches").doc(matchId).get();
  if (!matchDoc.exists) {
    throw new functions.https.HttpsError(
      "not-found",
      "Match not found"
    );
  }

  const match = matchDoc.data();
  
  // Check if player is part of this match
  if (match.player1Id !== context.auth.uid && 
      match.player2Id !== context.auth.uid) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Not a participant in this match"
    );
  }

  // Validate match status
  if (match.status !== "pending") {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "Match is not pending"
    );
  }

  // Validate match expiry
  if (match.expiresAt.toMillis() < Date.now()) {
    throw new functions.https.HttpsError(
      "deadline-exceeded",
      "Match has expired"
    );
  }

  // Validate result structure
  if (!validateCombatResult(result, match)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Invalid combat result"
    );
  }

  // Record result
  await matchDoc.ref.update({
    status: "completed",
    result: result,
    completedAt: admin.firestore.FieldValue.serverTimestamp(),
    submittedBy: context.auth.uid
  });

  // Update player stats
  const winnerId = result.playerWon ? match.player1Id : match.player2Id;
  const loserId = result.playerWon ? match.player2Id : match.player1Id;

  await Promise.all([
    updatePlayerStats(winnerId, true, result.xpGained),
    updatePlayerStats(loserId, false, 0)
  ]);

  return { success: true };
});

/**
 * Helper: Validate combat result
 */
function validateCombatResult(result, match) {
  // Check turn count
  if (result.turnCount < MIN_COMBAT_TURNS || 
      result.turnCount > MAX_COMBAT_TURNS) {
    return false;
  }

  // Verify proper turn alternation
  let lastActorId = null;
  for (const action of result.actionHistory) {
    if (action.sourceId === lastActorId) return false;
    lastActorId = action.sourceId;
  }

  // Verify final states
  if (result.playerWon) {
    if (!result.finalPlayerState.isAlive || 
        result.finalEnemyState.isAlive) {
      return false;
    }
  } else {
    if (result.finalPlayerState.isAlive || 
        !result.finalEnemyState.isAlive) {
      return false;
    }
  }

  return true;
}

/**
 * Helper: Update player stats after match
 */
async function updatePlayerStats(playerId, won, xpGained) {
  const playerRef = db.collection("players").doc(playerId);
  await playerRef.update({
    pvpWins: admin.firestore.FieldValue.increment(won ? 1 : 0),
    pvpLosses: admin.firestore.FieldValue.increment(won ? 0 : 1),
    totalXp: admin.firestore.FieldValue.increment(xpGained),
    lastPvpMatch: admin.firestore.FieldValue.serverTimestamp()
  });
}