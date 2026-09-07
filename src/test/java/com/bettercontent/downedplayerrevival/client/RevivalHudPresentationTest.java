package com.bettercontent.downedplayerrevival.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevivalHudPresentationTest {
    @Test
    void ordinaryDownedStateWaitsForHelp() {
        RevivalHudPresentation.View view = resolve(1200, 40, 0, 0, 0, 200, 20);

        assertEquals(RevivalHudPresentation.BannerState.DOWNED, view.banner());
        assertFalse(view.reviving());
        assertEquals(RevivalHudPresentation.GiveUpState.LOCKED, view.giveUp());
        assertEquals(160, view.giveUpLockedTicks());
    }

    @Test
    void activeHelperSelectsRevivalStateAndBoundsProgress() {
        RevivalHudPresentation.View view = resolve(800, 240, 150, 2, 0, 0, 20);

        assertEquals(RevivalHudPresentation.BannerState.REVIVING, view.banner());
        assertTrue(view.reviving());
        assertEquals(1.0f, view.reviveProgress());
        assertEquals(2, view.helperCount());
    }

    @Test
    void finalTenSecondsStayCriticalEvenWhileBeingRevived() {
        RevivalHudPresentation.View view = resolve(200, 1000, 50, 1, 0, 0, 20);

        assertEquals(RevivalHudPresentation.BannerState.CRITICAL, view.banner());
        assertTrue(view.reviving());
        assertEquals(0.5f, view.reviveProgress());
    }

    @Test
    void giveUpTransitionsFromReadyToHolding() {
        RevivalHudPresentation.View ready = resolve(800, 200, 0, 0, 0, 0, 20);
        RevivalHudPresentation.View holding = resolve(800, 200, 0, 0, 10, 0, 20);

        assertEquals(RevivalHudPresentation.GiveUpState.READY, ready.giveUp());
        assertEquals(RevivalHudPresentation.GiveUpState.HOLDING, holding.giveUp());
        assertEquals(0.5f, holding.giveUpProgress());
    }

    @Test
    void timerRoundsUpAndNeverDisplaysNegativeTime() {
        assertEquals("0:01", RevivalHudPresentation.formatTicks(1));
        assertEquals("1:00", RevivalHudPresentation.formatTicks(1200));
        assertEquals("0:00", RevivalHudPresentation.formatTicks(-20));
    }

    private static RevivalHudPresentation.View resolve(
            int ticksLeft,
            int downedTicks,
            float reviveProgress,
            int helperCount,
            int giveUpTicks,
            int giveUpUnlockTicks,
            int giveUpHoldTicks
    ) {
        return RevivalHudPresentation.resolve(
                ticksLeft,
                downedTicks,
                reviveProgress,
                helperCount,
                giveUpTicks,
                100,
                giveUpUnlockTicks,
                giveUpHoldTicks
        );
    }
}
