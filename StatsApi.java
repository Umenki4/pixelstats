package com.pixelstats.stats;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/** Small helper to read a player's stats. Never returns null (gives an empty throw-away object if missing). */
public final class StatsApi {
    public static final Capability<PlayerStats> CAP = CapabilityManager.get(new CapabilityToken<PlayerStats>() {});

    private StatsApi() {}

    public static PlayerStats get(Player player) {
        return player.getCapability(CAP).resolve().orElseGet(PlayerStats::new);
    }
}
