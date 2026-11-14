package com.pixelstats.client;

import com.pixelstats.stats.StatMath;
import com.pixelstats.stats.StatType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fml.ModList;

/** Builds the texts shown in the status window (row summary + hover tooltip). */
public final class StatTexts {
    private StatTexts() {}

    private static String pct(double fraction) {
        return String.format(Locale.ROOT, "%.1f%%", fraction * 100.0);
    }

    private static String num(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** Current value of every effect of one stat, in the same order as the lang keys e1, e2, e3... */
    public static String[] values(StatType type, int p) {
        return switch (type) {
            case DEX -> new String[] {pct(StatMath.dexDodge(p)), "+" + pct(StatMath.dexRanged(p)), "+" + pct(StatMath.dexSpeed(p))};
            case STR -> new String[] {"+" + num(StatMath.strDamage(p)), "+" + num(StatMath.strHealth(p))};
            case CON -> new String[] {"+" + num(StatMath.conHealth(p)), num(StatMath.conRegen(p)),
                    pct(StatMath.conEffectResist(p)), pct(StatMath.conKnockback(p))};
            case INT -> new String[] {"+" + pct(StatMath.intSpellPower(p)), "+" + num(StatMath.intMaxMana(p)),
                    "+" + pct(StatMath.intManaRegen(p))};
            case LUC -> new String[] {pct(StatMath.lucCrit(p)), "+" + pct(StatMath.lucCritDamage(p)), pct(StatMath.lucXpChance(p))};
        };
    }

    /** Short one-line summary drawn under the stat name. */
    public static Component summary(StatType type, int p) {
        String[] v = values(type, p);
        String key = "stat.pixelstats." + type.key() + ".s";
        return switch (type) {
            case DEX, STR, INT -> Component.translatable(key, v[0], v[1]);
            case CON -> Component.translatable(key, v[0], v[2]);
            case LUC -> Component.translatable(key, v[0], v[2]);
        };
    }

    public static List<Component> tooltip(StatType type, int p) {
        String base = "stat.pixelstats." + type.key();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(base)
                .withStyle(Style.EMPTY.withColor(type.color & 0xFFFFFF).withBold(true)));
        lines.add(Component.translatable(base + ".desc").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lines.add(Component.empty());
        String[] v = values(type, p);
        for (int i = 0; i < v.length; i++) {
            lines.add(Component.translatable(base + ".e" + (i + 1), v[i]).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.empty());
        lines.add(Component.translatable("stat.pixelstats.points", p).withStyle(ChatFormatting.YELLOW));
        if (type == StatType.INT) {
            boolean found = ModList.get().isLoaded("irons_spellbooks");
            lines.add(Component.translatable(found ? "stat.pixelstats.irons_found" : "stat.pixelstats.irons_missing")
                    .withStyle(found ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        return lines;
    }
}
