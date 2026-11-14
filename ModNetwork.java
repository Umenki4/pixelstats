package com.pixelstats.network;

import com.pixelstats.PixelStatsMod;
import com.pixelstats.stats.StatsApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PixelStatsMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncStatsPacket.class,
                SyncStatsPacket::encode, SyncStatsPacket::decode, SyncStatsPacket::handle);
        CHANNEL.registerMessage(id++, UpgradeStatPacket.class,
                UpgradeStatPacket::encode, UpgradeStatPacket::decode, UpgradeStatPacket::handle);
    }

    /** Sends the player's points to that player's client. */
    public static void syncTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncStatsPacket(StatsApi.get(player).toArray()));
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
