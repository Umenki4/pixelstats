package com.pixelstats.stats;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** Points a player has invested in each stat. Stored as a Forge capability on the player. */
public class PlayerStats {
    private final int[] points = new int[StatType.COUNT];

    public int get(StatType type) {
        return points[type.ordinal()];
    }

    public void set(StatType type, int value) {
        points[type.ordinal()] = Math.max(0, value);
    }

    public void add(StatType type, int amount) {
        long result = (long) get(type) + amount;
        set(type, (int) Math.max(0L, Math.min(Integer.MAX_VALUE, result)));
    }

    public void resetAll() {
        java.util.Arrays.fill(points, 0);
    }

    public int[] toArray() {
        return points.clone();
    }

    public void fromArray(int[] data) {
        for (int i = 0; i < points.length; i++) {
            points[i] = i < data.length ? Math.max(0, data[i]) : 0;
        }
    }

    public void copyFrom(PlayerStats other) {
        fromArray(other.points);
    }

    public long total() {
        long sum = 0;
        for (int p : points) {
            sum += p;
        }
        return sum;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("Points", points);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Points", Tag.TAG_INT_ARRAY)) {
            fromArray(tag.getIntArray("Points"));
        }
    }
}
