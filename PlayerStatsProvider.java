package com.pixelstats.stats;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/** Attaches PlayerStats to players and saves/loads it with the player data. */
public class PlayerStatsProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerStats stats = new PlayerStats();
    private LazyOptional<PlayerStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap != StatsApi.CAP) {
            return LazyOptional.empty();
        }
        if (!optional.isPresent()) {
            // After death Forge invalidates the old player's data and then revives it (reviveCaps) so it can be
            // copied to the new body. Our LazyOptional must come back to life too, otherwise the copy reads "empty".
            optional = LazyOptional.of(() -> stats);
        }
        return optional.cast();
    }

    @Override
    public CompoundTag serializeNBT() {
        return stats.save();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        stats.load(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
