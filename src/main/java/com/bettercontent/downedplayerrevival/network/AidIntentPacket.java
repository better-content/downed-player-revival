package com.bettercontent.downedplayerrevival.network;

import com.bettercontent.downedplayerrevival.RevivalManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record AidIntentPacket(UUID targetId, boolean active) {
    public static void encode(AidIntentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.targetId);
        buffer.writeBoolean(packet.active);
    }

    public static AidIntentPacket decode(FriendlyByteBuf buffer) {
        return new AidIntentPacket(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(AidIntentPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) context.enqueueWork(() -> RevivalManager.setAidIntent(sender, packet.targetId, packet.active));
        context.setPacketHandled(true);
    }
}
