package com.pixelstats.stats;

import com.pixelstats.config.PixelConfig;
import com.pixelstats.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;

/** Server-side rules for buying stat points with Minecraft XP levels. */
public final class StatActions {
    private StatActions() {}

    /** Buys up to {@code requested} points of one stat. Returns how many were actually bought. */
    public static int tryUpgrade(ServerPlayer player, StatType type, int requested) {
        int wanted = Math.max(1, Math.min(requested, 100));
        PlayerStats stats = StatsApi.get(player);

        int room = PixelConfig.get(PixelConfig.MAX_POINTS) - stats.get(type);
        int amount = Math.min(wanted, room);

        int cost = Math.max(1, PixelConfig.get(PixelConfig.LEVEL_COST));
        boolean free = player.isCreative() && PixelConfig.get(PixelConfig.CREATIVE_FREE);
        if (!free) {
            amount = Math.min(amount, player.experienceLevel / cost);
        }

        if (amount <= 0) {
            ModNetwork.syncTo(player);      // make the client re-check its numbers
            return 0;
        }
        if (!free) {
            player.giveExperienceLevels(-amount * cost);
        }
        stats.add(type, amount);
        StatEffects.apply(player);
        ModNetwork.syncTo(player);
        return amount;
    }
}
