package com.bettercontent.revival.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class PlayerFinishedEvent extends PlayerEvent {
    private final ServerPlayer attacker;

    public PlayerFinishedEvent(ServerPlayer player, ServerPlayer attacker) {
        super(player);
        this.attacker = attacker;
    }

    public ServerPlayer getAttacker() { return attacker; }
}
