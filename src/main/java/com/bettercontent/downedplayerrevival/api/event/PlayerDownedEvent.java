package com.bettercontent.downedplayerrevival.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class PlayerDownedEvent extends PlayerEvent {
    private final DamageSource source;

    public PlayerDownedEvent(ServerPlayer player, DamageSource source) {
        super(player);
        this.source = source;
    }

    public DamageSource getSource() { return source; }
}
