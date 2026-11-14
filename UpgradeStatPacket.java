package com.pixelstats.network;

import com.pixelstats.stats.StatActions;
import com.pixelstats.stats.StatType;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Client -> server: "please buy {amount} points of stat {statId}". The server validates everything. */
public class UpgradeStatPacket {
    private final int statId;
    private final int amount;

    public UpgradeStatPacket(int statId, int amount) {
        this.statId = statId;
        this.amount = amount;
    }

    public static void encode(UpgradeStatPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.statId);
        buf.writeVarInt(msg.amount);
    }

    public static UpgradeStatPacket decode(FriendlyByteBuf buf) {
        return new UpgradeStatPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(UpgradeStatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            StatType type = StatType.byId(msg.statId);
            if (player != null && type != null) {
                StatActions.tryUpgrade(player, type, msg.amount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
