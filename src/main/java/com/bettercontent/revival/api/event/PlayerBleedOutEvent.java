package com.bettercontent.revival.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class PlayerBleedOutEvent extends PlayerEvent {
    public PlayerBleedOutEvent(ServerPlayer player) { super(player); }
}
