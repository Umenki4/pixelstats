package com.pixelstats.client;

import net.minecraft.client.Minecraft;

/** Runs on the client only (called through DistExecutor so a dedicated server never loads it). */
public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void onSync(int[] data) {
        int[] before = ClientStats.snapshot();
        ClientStats.set(data);
        if (Minecraft.getInstance().screen instanceof StatsScreen screen) {
            screen.onStatsChanged(before, ClientStats.snapshot());
        }
    }
}
