package com.bettercontent.downedplayerrevival.client;

import com.bettercontent.downedplayerrevival.network.StateSyncPacket;
import com.bettercontent.downedplayerrevival.state.DownedPlayerConstraints;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ClientRevivalState {
    private static final Map<UUID, StateSyncPacket> STATES = new HashMap<>();

    private ClientRevivalState() {}

    public static void accept(StateSyncPacket packet) {
        if (packet.downed()) {
            STATES.put(packet.playerId(), packet);
            forcePose(packet.playerId());
        } else {
            STATES.remove(packet.playerId());
            clearPose(packet.playerId());
            ClientRevivalInput.targetCleared(packet.playerId());
        }
    }

    public static Optional<StateSyncPacket> get(UUID playerId) {
        return Optional.ofNullable(STATES.get(playerId));
    }

    public static boolean isDowned(UUID playerId) {
        return STATES.containsKey(playerId);
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }
        STATES.entrySet().removeIf(entry -> {
            Player player = minecraft.level.getPlayerByUUID(entry.getKey());
            if (player == null) return true;
            DownedPlayerConstraints.forcePronePose(player);
            return false;
        });
    }

    public static void clear() {
        STATES.keySet().forEach(ClientRevivalState::clearPose);
        STATES.clear();
        ClientRevivalInput.reset();
    }

    private static void clearPose(UUID playerId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Player player = minecraft.level.getPlayerByUUID(playerId);
        if (player != null && player.getForcedPose() == Pose.SWIMMING) player.setForcedPose(null);
    }

    private static void forcePose(UUID playerId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Player player = minecraft.level.getPlayerByUUID(playerId);
        if (player != null) DownedPlayerConstraints.forcePronePose(player);
    }
}
