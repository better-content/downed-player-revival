package com.bettercontent.downedplayerrevival.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RevivalStateTest {
    private static final RevivalTuning TUNING = new RevivalTuning(100, 20, 0.5f, 100, 100);

    @Test
    void unaidedPlayerBleedsOutAfterSixtySeconds() {
        RevivalState state = new RevivalState(1200, "minecraft:mob_attack");
        RevivalState.TickResult result = RevivalState.TickResult.ACTIVE;
        for (int i = 0; i < 1200; i++) result = state.tick(0, false, TUNING);
        assertEquals(RevivalState.TickResult.BLED_OUT, result);
    }

    @Test
    void oneHelperRevivesInFiveSecondsAndPausesBleeding() {
        RevivalState state = new RevivalState(1200, "minecraft:mob_attack");
        RevivalState.TickResult result = RevivalState.TickResult.ACTIVE;
        for (int i = 0; i < 100; i++) result = state.tick(1, false, TUNING);
        assertEquals(RevivalState.TickResult.REVIVED, result);
        assertEquals(1200, state.ticksLeft());
    }

    @Test
    void helpersStackAdditively() {
        RevivalState state = new RevivalState(1200, "minecraft:mob_attack");
        RevivalState.TickResult result = RevivalState.TickResult.ACTIVE;
        for (int i = 0; i < 50; i++) result = state.tick(2, false, TUNING);
        assertEquals(RevivalState.TickResult.REVIVED, result);
    }

    @Test
    void interruptedProgressWaitsThenDecaysAtHalfRate() {
        RevivalState state = new RevivalState(1200, "minecraft:mob_attack");
        for (int i = 0; i < 40; i++) state.tick(1, false, TUNING);
        for (int i = 0; i < 20; i++) state.tick(0, false, TUNING);
        assertEquals(40.0f, state.reviveProgress());
        for (int i = 0; i < 20; i++) state.tick(0, false, TUNING);
        assertEquals(30.0f, state.reviveProgress());
    }

    @Test
    void giveUpRequiresUnlockAndFullHold() {
        RevivalState state = new RevivalState(1200, "minecraft:fall");
        for (int i = 0; i < 99; i++) assertEquals(RevivalState.TickResult.ACTIVE, state.tick(0, true, TUNING));
        RevivalState.TickResult result = RevivalState.TickResult.ACTIVE;
        for (int i = 0; i < 100; i++) result = state.tick(0, true, TUNING);
        assertEquals(RevivalState.TickResult.GAVE_UP, result);
    }

    @Test
    void stateRoundTripsThroughNbt() {
        RevivalState original = new RevivalState(1200, "minecraft:lava");
        for (int i = 0; i < 37; i++) original.tick(1, false, TUNING);
        RevivalState restored = RevivalState.load(original.save());
        assertEquals(original.ticksLeft(), restored.ticksLeft());
        assertEquals(original.downedTicks(), restored.downedTicks());
        assertEquals(original.reviveProgress(), restored.reviveProgress());
        assertEquals(original.originalDamageType(), restored.originalDamageType());
    }
}
