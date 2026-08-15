package com.bettercontent.downedplayerrevival.state;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class DownedPlayerConstraintsTest {
    @Test
    void removesUpwardMotionWithoutChangingCrawlOrFalls() {
        assertEquals(new Vec3(0.2, 0.0, -0.1),
                DownedPlayerConstraints.constrainMotion(new Vec3(0.2, 0.42, -0.1)));

        Vec3 falling = new Vec3(0.2, -0.3, -0.1);
        assertSame(falling, DownedPlayerConstraints.constrainMotion(falling));
    }
}
