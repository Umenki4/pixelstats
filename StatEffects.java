package com.pixelstats.stats;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Turns invested points into real attribute modifiers on the player.
 * Modifiers are PERMANENT (saved with the player) so current health is not clamped when re-logging.
 * apply() is idempotent: call it any time the points change.
 */
public final class StatEffects {
    private static final UUID STR_ATTACK = UUID.fromString("5d1e9a40-0001-4c7e-9b1a-70a1c0de0001");
    private static final UUID STR_HEALTH = UUID.fromString("5d1e9a40-0002-4c7e-9b1a-70a1c0de0002");
    private static final UUID CON_HEALTH = UUID.fromString("5d1e9a40-0003-4c7e-9b1a-70a1c0de0003");
    private static final UUID CON_KNOCKBACK = UUID.fromString("5d1e9a40-0004-4c7e-9b1a-70a1c0de0004");
    private static final UUID DEX_SPEED = UUID.fromString("5d1e9a40-0005-4c7e-9b1a-70a1c0de0005");
    private static final UUID INT_POWER = UUID.fromString("5d1e9a40-0006-4c7e-9b1a-70a1c0de0006");
    private static final UUID INT_MANA = UUID.fromString("5d1e9a40-0007-4c7e-9b1a-70a1c0de0007");
    private static final UUID INT_REGEN = UUID.fromString("5d1e9a40-0008-4c7e-9b1a-70a1c0de0008");

    private static final String IRONS = "irons_spellbooks";

    private StatEffects() {}

    public static boolean ironsSpellsPresent() {
        return ForgeRegistries.ATTRIBUTES.containsKey(new ResourceLocation(IRONS, "spell_power"));
    }

    public static void apply(ServerPlayer player) {
        PlayerStats stats = StatsApi.get(player);
        int dex = stats.get(StatType.DEX);
        int str = stats.get(StatType.STR);
        int con = stats.get(StatType.CON);
        int intel = stats.get(StatType.INT);

        float oldMax = player.getMaxHealth();

        set(player.getAttribute(Attributes.ATTACK_DAMAGE), STR_ATTACK, "PixelStats STR damage",
                StatMath.strDamage(str), Operation.ADDITION);
        set(player.getAttribute(Attributes.MAX_HEALTH), STR_HEALTH, "PixelStats STR health",
                StatMath.strHealth(str), Operation.ADDITION);
        set(player.getAttribute(Attributes.MAX_HEALTH), CON_HEALTH, "PixelStats CON health",
                StatMath.conHealth(con), Operation.ADDITION);
        set(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), CON_KNOCKBACK, "PixelStats CON knockback",
                StatMath.conKnockback(con), Operation.ADDITION);
        set(player.getAttribute(Attributes.MOVEMENT_SPEED), DEX_SPEED, "PixelStats DEX speed",
                StatMath.dexSpeed(dex), Operation.MULTIPLY_BASE);

        // Iron's Spells 'n Spellbooks - optional, looked up by name so there is no compile/runtime dependency.
        set(ironsAttribute(player, "spell_power"), INT_POWER, "PixelStats INT spell power",
                StatMath.intSpellPower(intel), Operation.MULTIPLY_BASE);
        set(ironsAttribute(player, "max_mana"), INT_MANA, "PixelStats INT max mana",
                StatMath.intMaxMana(intel), Operation.ADDITION);
        set(ironsAttribute(player, "mana_regen"), INT_REGEN, "PixelStats INT mana regen",
                StatMath.intManaRegen(intel), Operation.MULTIPLY_BASE);

        float newMax = player.getMaxHealth();
        if (player.isAlive()) {
            if (newMax > oldMax) {
                player.heal(newMax - oldMax);          // reward: new max HP is filled immediately
            } else if (player.getHealth() > newMax) {
                player.setHealth(newMax);
            }
        }
    }

    private static AttributeInstance ironsAttribute(ServerPlayer player, String name) {
        ResourceLocation id = new ResourceLocation(IRONS, name);
        if (!ForgeRegistries.ATTRIBUTES.containsKey(id)) {
            return null;
        }
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
        return attribute == null ? null : player.getAttribute(attribute);
    }

    private static void set(AttributeInstance instance, UUID id, String name, double amount, Operation op) {
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            if (existing.getAmount() == amount && existing.getOperation() == op) {
                return;
            }
            instance.removeModifier(id);
        }
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, name, amount, op));
        }
    }
}
