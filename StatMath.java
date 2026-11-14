package com.pixelstats.stats;

import static com.pixelstats.config.PixelConfig.get;

import com.pixelstats.config.PixelConfig;

/**
 * All stat formulas in one place (used by the server for the real effect and by the client for the UI text).
 * Linear stats grow a little with every point forever.
 * Chance stats use  max * p / (p + half)  -> endless growth that can never exceed the cap.
 */
public final class StatMath {
    private StatMath() {}

    private static double hyper(int points, double max, double half) {
        return points <= 0 ? 0.0 : max * points / (points + half);
    }

    // ---- STR
    public static double strDamage(int p) { return p * get(PixelConfig.STR_DAMAGE); }
    public static double strHealth(int p) { return p * get(PixelConfig.STR_HEALTH); }

    // ---- CON
    public static double conHealth(int p) { return p * get(PixelConfig.CON_HEALTH); }
    public static double conRegen(int p) { return p * get(PixelConfig.CON_REGEN); }
    public static double conEffectResist(int p) { return hyper(p, get(PixelConfig.CON_RESIST_MAX), get(PixelConfig.CON_RESIST_HALF)); }
    public static double conKnockback(int p) { return Math.min(0.8, p * 0.002); }

    // ---- DEX
    public static double dexDodge(int p) { return hyper(p, get(PixelConfig.DEX_DODGE_MAX), get(PixelConfig.DEX_DODGE_HALF)); }
    public static double dexRanged(int p) { return p * get(PixelConfig.DEX_RANGED); }
    public static double dexSpeed(int p) { return hyper(p, get(PixelConfig.DEX_SPEED_MAX), get(PixelConfig.DEX_SPEED_HALF)); }

    // ---- INT (Iron's Spells)
    public static double intSpellPower(int p) { return p * get(PixelConfig.INT_SPELL_POWER); }
    public static double intMaxMana(int p) { return p * get(PixelConfig.INT_MAX_MANA); }
    public static double intManaRegen(int p) { return p * get(PixelConfig.INT_MANA_REGEN); }

    // ---- LUC
    public static double lucCrit(int p) { return hyper(p, get(PixelConfig.LUC_CRIT_MAX), get(PixelConfig.LUC_CRIT_HALF)); }
    /** Extra crit damage as a fraction: 0.4 = crits hit 40% harder than normal. */
    public static double lucCritDamage(int p) { return hyper(p, get(PixelConfig.LUC_CRIT_DAMAGE_MAX), get(PixelConfig.LUC_CRIT_DAMAGE_HALF)); }
    /** Chance that a kill drops bonus XP. */
    public static double lucXpChance(int p) { return hyper(p, get(PixelConfig.LUC_XP_CHANCE_MAX), get(PixelConfig.LUC_XP_CHANCE_HALF)); }
}
