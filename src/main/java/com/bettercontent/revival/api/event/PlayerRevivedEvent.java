package com.bettercontent.revival.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class PlayerRevivedEvent extends PlayerEvent {
    public PlayerRevivedEvent(ServerPlayer player) { super(player); }
}
