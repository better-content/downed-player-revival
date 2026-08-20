package com.bettercontent.downedplayerrevival.gametest;

import com.bettercontent.downedplayerrevival.RevivalConfig;
import com.bettercontent.downedplayerrevival.RevivalMod;
import com.bettercontent.downedplayerrevival.state.DownedPlayerConstraints;
import com.bettercontent.downedplayerrevival.state.RevivalState;
import com.bettercontent.downedplayerrevival.state.RevivalTuning;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(RevivalMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RevivalGameTests {
    private RevivalGameTests() {}

    @GameTest(template = "empty")
    public static void authoredTimingAndDamageTypesLoad(GameTestHelper helper) {
        if (RevivalConfig.BLEED_TICKS.get() != 1200
                || RevivalConfig.REVIVE_TICKS.get() != 100
                || RevivalConfig.GIVE_UP_UNLOCK_TICKS.get() != 0
                || RevivalConfig.GIVE_UP_HOLD_TICKS.get() != 1) {
            helper.fail("Revival authored timing defaults changed");
            return;
        }
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        if (!registry.containsKey(new ResourceLocation("downed_player_revival", "bled_out"))
                || !registry.containsKey(new ResourceLocation("downed_player_revival", "finished"))) {
            helper.fail("Revival damage types did not load");
            return;
        }
        RevivalState state = new RevivalState(1200, "minecraft:fall");
        RevivalTuning tuning = new RevivalTuning(100, 20, 0.5f, 0, 1);
        for (int i = 0; i < 49; i++) state.tick(2, false, tuning);
        if (state.tick(2, false, tuning) != RevivalState.TickResult.REVIVED) {
            helper.fail("Two helpers did not complete additive downed_player_revival at 50 ticks");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void downedPhysicalConstraintsAreAuthoritative(GameTestHelper helper) {
        var player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "downed-constraint-probe"));
        player.setSprinting(true);
        player.setShiftKeyDown(true);
        player.setDeltaMovement(0.2, 0.42, -0.1);
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;

        DownedPlayerConstraints.enforce(player);

        if (player.getForcedPose() != Pose.SWIMMING
                || player.isSprinting()
                || player.isShiftKeyDown()
                || player.getAbilities().flying
                || !player.getDeltaMovement().equals(new Vec3(0.2, 0.0, -0.1))) {
            helper.fail("Downed physical constraints were not applied consistently");
            return;
        }
        helper.succeed();
    }
}
