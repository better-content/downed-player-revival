package com.bettercontent.downedplayerrevival.network;

import com.bettercontent.downedplayerrevival.RevivalManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record FinishPacket(UUID targetId) {
    public static void encode(FinishPacket packet, FriendlyByteBuf buffer) { buffer.writeUUID(packet.targetId); }
    public static FinishPacket decode(FriendlyByteBuf buffer) { return new FinishPacket(buffer.readUUID()); }

    public static void handle(FinishPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) context.enqueueWork(() -> RevivalManager.finish(sender, packet.targetId));
        context.setPacketHandled(true);
    }
}
