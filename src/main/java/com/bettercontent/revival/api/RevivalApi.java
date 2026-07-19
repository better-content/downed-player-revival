package com.bettercontent.revival.api;

import com.bettercontent.revival.state.RevivalState;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class RevivalApi {
    private RevivalApi() {}

    public static boolean isDowned(Player player) {
        return player.getPersistentData().contains(RevivalState.ROOT_TAG);
    }

    public static Optional<Snapshot> snapshot(Player player) {
        if (!isDowned(player)) return Optional.empty();
        RevivalState state = RevivalState.load(player.getPersistentData().getCompound(RevivalState.ROOT_TAG));
        return Optional.of(new Snapshot(state.ticksLeft(), state.downedTicks(), state.reviveProgress(), state.giveUpTicks()));
    }

    public record Snapshot(int ticksLeft, int downedTicks, float reviveProgress, int giveUpTicks) {}
}
