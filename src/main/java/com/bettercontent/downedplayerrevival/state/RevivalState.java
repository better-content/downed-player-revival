package com.bettercontent.downedplayerrevival.state;

import net.minecraft.nbt.CompoundTag;

public final class RevivalState {
    public static final String ROOT_TAG = "downed_player_revival:state";

    private int ticksLeft;
    private int downedTicks;
    private float reviveProgress;
    private int unaidedTicks;
    private int giveUpTicks;
    private String originalDamageType;

    public RevivalState(int ticksLeft, String originalDamageType) {
        this.ticksLeft = ticksLeft;
        this.originalDamageType = originalDamageType == null ? "" : originalDamageType;
    }

    public TickResult tick(int helperCount, boolean givingUp, RevivalTuning tuning) {
        downedTicks++;
        if (helperCount > 0) {
            unaidedTicks = 0;
            reviveProgress += helperCount;
        } else {
            ticksLeft--;
            unaidedTicks++;
            if (unaidedTicks > tuning.decayGraceTicks()) {
                reviveProgress = Math.max(0.0f, reviveProgress - tuning.decayPerTick());
            }
        }

        if (givingUp && downedTicks >= tuning.giveUpUnlockTicks()) {
            giveUpTicks++;
        } else {
            giveUpTicks = 0;
        }

        if (giveUpTicks >= tuning.giveUpHoldTicks()) return TickResult.GAVE_UP;
        if (reviveProgress >= tuning.reviveTicks()) return TickResult.REVIVED;
        if (ticksLeft <= 0) return TickResult.BLED_OUT;
        return TickResult.ACTIVE;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ticksLeft", ticksLeft);
        tag.putInt("downedTicks", downedTicks);
        tag.putFloat("reviveProgress", reviveProgress);
        tag.putInt("unaidedTicks", unaidedTicks);
        tag.putInt("giveUpTicks", giveUpTicks);
        tag.putString("originalDamageType", originalDamageType);
        return tag;
    }

    public static RevivalState load(CompoundTag tag) {
        RevivalState state = new RevivalState(tag.getInt("ticksLeft"), tag.getString("originalDamageType"));
        state.downedTicks = tag.getInt("downedTicks");
        state.reviveProgress = tag.getFloat("reviveProgress");
        state.unaidedTicks = tag.getInt("unaidedTicks");
        state.giveUpTicks = tag.getInt("giveUpTicks");
        return state;
    }

    public int ticksLeft() { return ticksLeft; }
    public int downedTicks() { return downedTicks; }
    public float reviveProgress() { return reviveProgress; }
    public int giveUpTicks() { return giveUpTicks; }
    public String originalDamageType() { return originalDamageType; }

    public enum TickResult { ACTIVE, REVIVED, GAVE_UP, BLED_OUT }
}
