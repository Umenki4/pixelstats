package com.pixelstats.client;

import com.pixelstats.stats.StatType;

/** Client-side copy of the local player's points (filled by SyncStatsPacket). Used only for drawing the UI. */
public final class ClientStats {
    private static int[] points = new int[StatType.COUNT];

    private ClientStats() {}

    public static int get(StatType type) {
        return points[type.ordinal()];
    }

    public static int[] snapshot() {
        return points.clone();
    }

    public static void set(int[] data) {
        int[] fresh = new int[StatType.COUNT];
        for (int i = 0; i < fresh.length && i < data.length; i++) {
            fresh[i] = Math.max(0, data[i]);
        }
        points = fresh;
    }

    public static void clear() {
        points = new int[StatType.COUNT];
    }

    public static long total() {
        long sum = 0;
        for (int p : points) {
            sum += p;
        }
        return sum;
    }
}
