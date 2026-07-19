package com.bettercontent.revival.gametest;

import com.bettercontent.revival.RevivalConfig;
import com.bettercontent.revival.RevivalMod;
import com.bettercontent.revival.state.RevivalState;
import com.bettercontent.revival.state.RevivalTuning;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RevivalGameTests {
    private RevivalGameTests() {}

    @GameTest(template = "empty")
    public static void authoredTimingAndDamageTypesLoad(GameTestHelper helper) {
        if (RevivalConfig.BLEED_TICKS.get() != 1200 || RevivalConfig.REVIVE_TICKS.get() != 100) {
            helper.fail("Revival authored timing defaults changed");
            return;
        }
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        if (!registry.containsKey(new ResourceLocation("revival", "bled_out"))
                || !registry.containsKey(new ResourceLocation("revival", "finished"))) {
            helper.fail("Revival damage types did not load");
            return;
        }
        RevivalState state = new RevivalState(1200, "minecraft:fall");
        RevivalTuning tuning = new RevivalTuning(100, 20, 0.5f, 100, 100);
        for (int i = 0; i < 49; i++) state.tick(2, false, tuning);
        if (state.tick(2, false, tuning) != RevivalState.TickResult.REVIVED) {
            helper.fail("Two helpers did not complete additive revival at 50 ticks");
            return;
        }
        helper.succeed();
    }
}
