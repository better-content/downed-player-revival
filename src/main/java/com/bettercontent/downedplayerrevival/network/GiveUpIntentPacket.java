package com.bettercontent.downedplayerrevival.network;

import com.bettercontent.downedplayerrevival.RevivalManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GiveUpIntentPacket(boolean active) {
    public static void encode(GiveUpIntentPacket packet, FriendlyByteBuf buffer) { buffer.writeBoolean(packet.active); }
    public static GiveUpIntentPacket decode(FriendlyByteBuf buffer) { return new GiveUpIntentPacket(buffer.readBoolean()); }

    public static void handle(GiveUpIntentPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) context.enqueueWork(() -> RevivalManager.setGiveUpIntent(sender, packet.active));
        context.setPacketHandled(true);
    }
}
