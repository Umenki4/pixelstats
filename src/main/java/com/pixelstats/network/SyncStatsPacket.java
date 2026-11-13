package com.pixelstats.network;

import com.pixelstats.client.ClientPacketHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Server -> client: the player's current points. */
public class SyncStatsPacket {
    private final int[] points;

    public SyncStatsPacket(int[] points) {
        this.points = points;
    }

    public static void encode(SyncStatsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.points.length);
        for (int p : msg.points) {
            buf.writeVarInt(p);
        }
    }

    public static SyncStatsPacket decode(FriendlyByteBuf buf) {
        int size = Math.max(0, Math.min(buf.readVarInt(), 64));
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = buf.readVarInt();
        }
        return new SyncStatsPacket(data);
    }

    public static void handle(SyncStatsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.onSync(msg.points)));
        ctx.get().setPacketHandled(true);
    }
}
