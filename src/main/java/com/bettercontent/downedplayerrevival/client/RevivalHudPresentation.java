package com.bettercontent.downedplayerrevival.client;

import java.util.Locale;

final class RevivalHudPresentation {
    static final int CRITICAL_TICKS = 10 * 20;

    private RevivalHudPresentation() {}

    static View resolve(
            int ticksLeft,
            int downedTicks,
            float reviveProgress,
            int helperCount,
            int giveUpTicks,
            int reviveTicks,
            int giveUpUnlockTicks,
            int giveUpHoldTicks
    ) {
        boolean reviving = reviveProgress > 0.0f || helperCount > 0;
        BannerState banner = ticksLeft <= CRITICAL_TICKS
                ? BannerState.CRITICAL
                : reviving ? BannerState.REVIVING : BannerState.DOWNED;

        GiveUpState giveUp;
        if (downedTicks < giveUpUnlockTicks) {
            giveUp = GiveUpState.LOCKED;
        } else if (giveUpTicks > 0) {
            giveUp = GiveUpState.HOLDING;
        } else {
            giveUp = GiveUpState.READY;
        }

        return new View(
                banner,
                reviving,
                boundedProgress(reviveProgress, reviveTicks),
                Math.max(0, helperCount),
                giveUp,
                boundedProgress(giveUpTicks, giveUpHoldTicks),
                Math.max(0, giveUpUnlockTicks - downedTicks),
                Math.max(0, ticksLeft)
        );
    }

    static String formatTicks(int ticks) {
        int seconds = Math.max(0, (ticks + 19) / 20);
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    private static float boundedProgress(float progress, int total) {
        if (total <= 0) return 0.0f;
        return Math.max(0.0f, Math.min(1.0f, progress / total));
    }

    enum BannerState {
        DOWNED,
        REVIVING,
        CRITICAL
    }

    enum GiveUpState {
        LOCKED,
        READY,
        HOLDING
    }

    record View(
            BannerState banner,
            boolean reviving,
            float reviveProgress,
            int helperCount,
            GiveUpState giveUp,
            float giveUpProgress,
            int giveUpLockedTicks,
            int ticksLeft
    ) {}
}
