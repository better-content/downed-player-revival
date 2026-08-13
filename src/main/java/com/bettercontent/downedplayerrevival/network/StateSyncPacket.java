package com.bettercontent.downedplayerrevival.network;

import com.bettercontent.downedplayerrevival.client.ClientRevivalState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record StateSyncPacket(
        UUID playerId,
        boolean downed,
        int ticksLeft,
        int downedTicks,
        float reviveProgress,
        int giveUpTicks,
        int helperCount
) {
    public static void encode(StateSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeBoolean(packet.downed);
        buffer.writeVarInt(packet.ticksLeft);
        buffer.writeVarInt(packet.downedTicks);
        buffer.writeFloat(packet.reviveProgress);
        buffer.writeVarInt(packet.giveUpTicks);
        buffer.writeVarInt(packet.helperCount);
    }

    public static StateSyncPacket decode(FriendlyByteBuf buffer) {
        return new StateSyncPacket(
                buffer.readUUID(), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readFloat(), buffer.readVarInt(), buffer.readVarInt()
        );
    }

    public static void handle(StateSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRevivalState.accept(packet)));
        context.setPacketHandled(true);
    }
}
