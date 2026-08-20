package com.bettercontent.downedplayerrevival;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RevivalConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue BLEED_TICKS;
    public static final ForgeConfigSpec.IntValue REVIVE_TICKS;
    public static final ForgeConfigSpec.DoubleValue REVIVE_DISTANCE;
    public static final ForgeConfigSpec.IntValue DECAY_GRACE_TICKS;
    public static final ForgeConfigSpec.DoubleValue DECAY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue HELPER_EXHAUSTION_PER_TICK;
    public static final ForgeConfigSpec.IntValue GIVE_UP_UNLOCK_TICKS;
    public static final ForgeConfigSpec.IntValue GIVE_UP_HOLD_TICKS;
    public static final ForgeConfigSpec.IntValue REVIVED_HEALTH_HALF_HEARTS;
    public static final ForgeConfigSpec.IntValue RECOVERY_TICKS;
    public static final ForgeConfigSpec.IntValue WHISPER_INTERVAL_TICKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("downed_player_revival");
        BLEED_TICKS = builder.comment("Ticks before an unaided player bleeds out.")
                .defineInRange("bleedTicks", 1200, 20, 12000);
        REVIVE_TICKS = builder.comment("Solo-helper ticks required for a full revive.")
                .defineInRange("reviveTicks", 100, 10, 1200);
        REVIVE_DISTANCE = builder.comment("Maximum helper and finisher distance in blocks.")
                .defineInRange("interactionDistance", 3.0, 1.0, 8.0);
        DECAY_GRACE_TICKS = builder.comment("Ticks before interrupted revive progress starts decaying.")
                .defineInRange("decayGraceTicks", 20, 0, 200);
        DECAY_PER_TICK = builder.comment("Revive progress lost per unaided tick after the grace period.")
                .defineInRange("decayPerTick", 0.5, 0.0, 10.0);
        HELPER_EXHAUSTION_PER_TICK = builder.comment("Exhaustion charged to each active helper per tick.")
                .defineInRange("helperExhaustionPerTick", 0.5, 0.0, 10.0);
        GIVE_UP_UNLOCK_TICKS = builder.comment("Downed ticks before give-up input becomes available.")
                .defineInRange("giveUpUnlockTicks", 0, 0, 1200);
        GIVE_UP_HOLD_TICKS = builder.comment("Continuous Sneak-hold ticks required to give up.")
                .defineInRange("giveUpHoldTicks", 1, 1, 1200);
        REVIVED_HEALTH_HALF_HEARTS = builder.comment("Health after downed_player_revival, measured in half-hearts.")
                .defineInRange("revivedHealthHalfHearts", 2, 1, 40);
        RECOVERY_TICKS = builder.comment("Weakness and slowness duration after downed_player_revival.")
                .defineInRange("recoveryTicks", 100, 0, 1200);
        WHISPER_INTERVAL_TICKS = builder.comment("Ticks between positional downed-player whispers.")
                .defineInRange("whisperIntervalTicks", 80, 20, 1200);
        builder.pop();
        SPEC = builder.build();
    }

    private RevivalConfig() {}
}
