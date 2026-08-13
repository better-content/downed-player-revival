package com.bettercontent.downedplayerrevival.network;

import com.bettercontent.downedplayerrevival.RevivalMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class RevivalNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RevivalMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private RevivalNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, StateSyncPacket.class, StateSyncPacket::encode, StateSyncPacket::decode, StateSyncPacket::handle);
        CHANNEL.registerMessage(id++, AidIntentPacket.class, AidIntentPacket::encode, AidIntentPacket::decode, AidIntentPacket::handle);
        CHANNEL.registerMessage(id++, GiveUpIntentPacket.class, GiveUpIntentPacket::encode, GiveUpIntentPacket::decode, GiveUpIntentPacket::handle);
        CHANNEL.registerMessage(id, FinishPacket.class, FinishPacket::encode, FinishPacket::decode, FinishPacket::handle);
    }
}
