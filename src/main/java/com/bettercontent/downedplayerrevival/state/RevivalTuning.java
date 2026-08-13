package com.bettercontent.downedplayerrevival.state;

public record RevivalTuning(
        int reviveTicks,
        int decayGraceTicks,
        float decayPerTick,
        int giveUpUnlockTicks,
        int giveUpHoldTicks
) {}
