package com.bettercontent.downedplayerrevival.state;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative physical and activity constraints for a downed player. */
public final class DownedPlayerConstraints {
    private DownedPlayerConstraints() {}

    public static void enforce(ServerPlayer player) {
        player.setForcedPose(Pose.SWIMMING);
        player.stopUsingItem();
        if (player.isPassenger()) player.stopRiding();
        boolean hasNetworkChannel = player.connection != null && player.connection.connection.channel() != null;
        if (player.containerMenu != player.inventoryMenu) {
            if (hasNetworkChannel) player.closeContainer();
            else player.doCloseContainer();
        }

        player.setSprinting(false);
        player.setShiftKeyDown(false);
        player.setDeltaMovement(constrainMotion(player.getDeltaMovement()));

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            if (hasNetworkChannel) player.onUpdateAbilities();
        }
    }

    public static Vec3 constrainMotion(Vec3 motion) {
        return motion.y > 0.0 ? new Vec3(motion.x, 0.0, motion.z) : motion;
    }
}
