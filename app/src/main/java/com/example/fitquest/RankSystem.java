package com.example.fitquest;

/**
 * FitQuest rank system based on rank points
 * Implements 5 ranks: Novice, Veteran, Elite, Hero, Legendary
 */
public class RankSystem {
    
    public enum Tier {
        NOVICE(0, "Novice", 0, 100),
        VETERAN(1, "Veteran", 101, 200),
        ELITE(2, "Elite", 201, 350),
        HERO(3, "Hero", 351, 499),
        LEGENDARY(4, "Legendary", 500, Integer.MAX_VALUE);
        
        private final int tierId;
        private final String name;
        private final int minRP;
        private final int maxRP;
        
        Tier(int tierId, String name, int minRP, int maxRP) {
            this.tierId = tierId;
            this.name = name;
            this.minRP = minRP;
            this.maxRP = maxRP;
        }
        
        public int getTierId() { return tierId; }
        public String getName() { return name; }
        public int getMinRP() { return minRP; }
        public int getMaxRP() { return maxRP; }
        
        public static Tier fromRankPoints(int rp) {
            for (Tier tier : values()) {
                if (rp >= tier.minRP && rp < tier.maxRP) {
                    return tier;
                }
            }
            return LEGENDARY; // Default to highest tier
        }
        
        public int getRPInTier(int totalRP) {
            return totalRP - minRP;
        }
        
        public int getRPToNextTier(int totalRP) {
            if (this == LEGENDARY) return 0;
            return maxRP - totalRP;
        }
    }
    
    public static class RankInfo {
        public final Tier tier;
        public final int rpInTier;
        public final int rpToNextTier;
        public final String displayName;
        public final int drawableRes;
        
        public RankInfo(Tier tier, int rpInTier, int rpToNextTier, String displayName, int drawableRes) {
            this.tier = tier;
            this.rpInTier = rpInTier;
            this.rpToNextTier = rpToNextTier;
            this.displayName = displayName;
            this.drawableRes = drawableRes;
        }
    }
    
    /**
     * Calculate rank info from total rank points
     */
    public static RankInfo getRankInfo(int totalRP) {
        Tier tier = Tier.fromRankPoints(totalRP);
        int rpInTier = tier.getRPInTier(totalRP);
        int rpToNextTier = tier.getRPToNextTier(totalRP);
        String displayName = tier.getName();
        int drawableRes = getRankIconResource(tier);
        
        return new RankInfo(tier, rpInTier, rpToNextTier, displayName, drawableRes);
    }
    
    /**
     * Calculate rank points gain/loss based on win/loss and current rank
     */
    public static int calculateRPGain(boolean won, int currentRP, int enemyRP) {
        int baseGain = won ? 25 : -10; // Base RP gain/loss
        
        // Adjust based on rank difference
        int rankDiff = enemyRP - currentRP;
        int adjustment = rankDiff / 100; // Adjust by rank difference
        
        if (won) {
            // Gain more RP for beating higher ranked opponents
            baseGain += adjustment;
        } else {
            // Lose less RP for losing to higher ranked opponents
            baseGain -= adjustment;
        }
        
        // Ensure minimum gains/losses
        if (won) {
            return Math.max(20, baseGain);
        } else {
            return Math.max(-15, baseGain);
        }
    }
    
    /**
     * Get rank icon resource based on tier
     */
    public static int getRankIconResource(Tier tier) {
        switch (tier) {
            case NOVICE: return R.drawable.rank_novice;
            case VETERAN: return R.drawable.rank_veteran;
            case ELITE: return R.drawable.rank_elite;
            case HERO: return R.drawable.rank_hero;
            case LEGENDARY: return R.drawable.rank_legendary;
            default: return R.drawable.rank_novice;
        }
    }
    
    /**
     * Get rank color based on tier
     */
    public static int getRankColor(Tier tier) {
        switch (tier) {
            case NOVICE: return 0xFF8B4513; // Brown
            case VETERAN: return 0xFFCD7F32; // Bronze
            case ELITE: return 0xFFC0C0C0; // Silver
            case HERO: return 0xFFFFD700; // Gold
            case LEGENDARY: return 0xFF800080; // Purple
            default: return 0xFF8B4513;
        }
    }
}
