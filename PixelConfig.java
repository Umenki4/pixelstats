package com.pixelstats.config;

import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server config (file: saves/<world>/serverconfig/pixelstats-server.toml, synced to clients on servers).
 * Every value falls back to its default if read before the config is loaded, so it can never crash.
 */
public final class PixelConfig {
    private static final Map<ForgeConfigSpec.ConfigValue<?>, Object> DEFAULTS = new HashMap<>();

    public static final ForgeConfigSpec SPEC;

    // general
    public static final ForgeConfigSpec.IntValue LEVEL_COST;
    public static final ForgeConfigSpec.IntValue MAX_POINTS;
    public static final ForgeConfigSpec.BooleanValue CREATIVE_FREE;
    public static final ForgeConfigSpec.BooleanValue KEEP_ON_DEATH;
    // STR
    public static final ForgeConfigSpec.DoubleValue STR_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue STR_HEALTH;
    // CON
    public static final ForgeConfigSpec.DoubleValue CON_HEALTH;
    public static final ForgeConfigSpec.DoubleValue CON_REGEN;
    public static final ForgeConfigSpec.DoubleValue CON_RESIST_MAX;
    public static final ForgeConfigSpec.DoubleValue CON_RESIST_HALF;
    // DEX
    public static final ForgeConfigSpec.DoubleValue DEX_DODGE_MAX;
    public static final ForgeConfigSpec.DoubleValue DEX_DODGE_HALF;
    public static final ForgeConfigSpec.DoubleValue DEX_RANGED;
    public static final ForgeConfigSpec.DoubleValue DEX_SPEED_MAX;
    public static final ForgeConfigSpec.DoubleValue DEX_SPEED_HALF;
    // INT
    public static final ForgeConfigSpec.DoubleValue INT_SPELL_POWER;
    public static final ForgeConfigSpec.DoubleValue INT_MAX_MANA;
    public static final ForgeConfigSpec.DoubleValue INT_MANA_REGEN;
    // LUC
    public static final ForgeConfigSpec.DoubleValue LUC_CRIT_MAX;
    public static final ForgeConfigSpec.DoubleValue LUC_CRIT_HALF;
    public static final ForgeConfigSpec.DoubleValue LUC_CRIT_DAMAGE_MAX;
    public static final ForgeConfigSpec.DoubleValue LUC_CRIT_DAMAGE_HALF;
    public static final ForgeConfigSpec.DoubleValue LUC_XP_CHANCE_MAX;
    public static final ForgeConfigSpec.DoubleValue LUC_XP_CHANCE_HALF;
    public static final ForgeConfigSpec.DoubleValue LUC_XP_BONUS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("general");
        LEVEL_COST = intVal(b, "levelCostPerUpgrade", "Minecraft XP levels paid for ONE point upgrade.", 1, 1, 1000);
        MAX_POINTS = intVal(b, "maxPointsPerStat", "Safety cap of points per stat (practically endless).", 1_000_000, 1, 2_000_000_000);
        CREATIVE_FREE = boolVal(b, "creativeUpgradesAreFree", "Creative-mode players do not pay XP levels.", true);
        KEEP_ON_DEATH = boolVal(b, "keepStatsOnDeath", "Keep invested stat points after dying.", true);
        b.pop();

        b.push("str");
        STR_DAMAGE = dbl(b, "attackDamagePerPoint", "Extra melee attack damage per point.", 0.05, 0, 100);
        STR_HEALTH = dbl(b, "maxHealthPerPoint", "Extra max HP per point (2 HP = 1 heart).", 0.10, 0, 100);
        b.pop();

        b.push("con");
        CON_HEALTH = dbl(b, "maxHealthPerPoint", "Extra max HP per point (2 HP = 1 heart).", 0.25, 0, 100);
        CON_REGEN = dbl(b, "regenPerPoint", "HP healed every 2 seconds per point.", 0.01, 0, 10);
        CON_RESIST_MAX = dbl(b, "effectResistMax", "Max chance (0-1) to resist a harmful potion effect.", 0.60, 0, 1);
        CON_RESIST_HALF = dbl(b, "effectResistHalfPoints", "Points needed to reach half of the max resist chance.", 200, 1, 1_000_000);
        b.pop();

        b.push("dex");
        DEX_DODGE_MAX = dbl(b, "dodgeChanceMax", "Max dodge chance (0-1). Never fully reached.", 0.50, 0, 1);
        DEX_DODGE_HALF = dbl(b, "dodgeHalfPoints", "Points needed to reach half of the max dodge chance.", 150, 1, 1_000_000);
        DEX_RANGED = dbl(b, "rangedDamagePerPoint", "Ranged damage bonus per point (0.005 = +0.5%).", 0.005, 0, 10);
        DEX_SPEED_MAX = dbl(b, "moveSpeedMax", "Max movement speed bonus (0.30 = +30%).", 0.30, 0, 5);
        DEX_SPEED_HALF = dbl(b, "moveSpeedHalfPoints", "Points needed to reach half of the max speed bonus.", 300, 1, 1_000_000);
        b.pop();

        b.push("int");
        INT_SPELL_POWER = dbl(b, "spellPowerPerPoint", "Iron's Spells spell power per point (0.01 = +1%).", 0.01, 0, 10);
        INT_MAX_MANA = dbl(b, "maxManaPerPoint", "Iron's Spells max mana per point.", 2.0, 0, 1000);
        INT_MANA_REGEN = dbl(b, "manaRegenPerPoint", "Iron's Spells mana regen per point (0.01 = +1%).", 0.01, 0, 10);
        b.pop();

        b.push("luc");
        LUC_CRIT_MAX = dbl(b, "critChanceMax", "Max extra crit chance (0-1), melee and ranged. Never fully reached.", 0.60, 0, 1);
        LUC_CRIT_HALF = dbl(b, "critHalfPoints", "Points needed to reach half of the max crit chance.", 120, 1, 1_000_000);
        LUC_CRIT_DAMAGE_MAX = dbl(b, "critDamageBonusMax", "Max EXTRA crit damage (1.5 = crits deal +150% of the normal crit damage). Never fully reached.", 1.5, 0, 100);
        LUC_CRIT_DAMAGE_HALF = dbl(b, "critDamageHalfPoints", "Points needed to reach half of the max extra crit damage.", 250, 1, 1_000_000);
        LUC_XP_CHANCE_MAX = dbl(b, "bonusXpChanceMax", "Max chance (0-1) that a mob you kill drops bonus XP. Never fully reached.", 0.50, 0, 1);
        LUC_XP_CHANCE_HALF = dbl(b, "bonusXpHalfPoints", "Points needed to reach half of the max bonus-XP chance.", 200, 1, 1_000_000);
        LUC_XP_BONUS = dbl(b, "bonusXpAmount", "Extra XP when the bonus triggers, as a fraction of the normal drop (1.0 = double XP).", 1.0, 0, 100);
        b.pop();

        SPEC = b.build();
    }

    private PixelConfig() {}

    @SuppressWarnings("unchecked")
    public static <T> T get(ForgeConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (RuntimeException notLoadedYet) {
            return (T) DEFAULTS.get(value);
        }
    }

    private static ForgeConfigSpec.IntValue intVal(ForgeConfigSpec.Builder b, String path, String comment, int def, int min, int max) {
        ForgeConfigSpec.IntValue v = b.comment(comment).defineInRange(path, def, min, max);
        DEFAULTS.put(v, def);
        return v;
    }

    private static ForgeConfigSpec.DoubleValue dbl(ForgeConfigSpec.Builder b, String path, String comment, double def, double min, double max) {
        ForgeConfigSpec.DoubleValue v = b.comment(comment).defineInRange(path, def, min, max);
        DEFAULTS.put(v, def);
        return v;
    }

    private static ForgeConfigSpec.BooleanValue boolVal(ForgeConfigSpec.Builder b, String path, String comment, boolean def) {
        ForgeConfigSpec.BooleanValue v = b.comment(comment).define(path, def);
        DEFAULTS.put(v, def);
        return v;
    }
}
