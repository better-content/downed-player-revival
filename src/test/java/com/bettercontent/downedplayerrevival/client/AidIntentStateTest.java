package com.bettercontent.downedplayerrevival.client;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class AidIntentStateTest {
    private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void acquiresTargetAfterUseWasAlreadyHeld() {
        AidIntentState state = new AidIntentState();
        assertEquals(List.of(), state.update(null));
        assertEquals(List.of(new AidIntentState.Update(FIRST, true)), state.update(FIRST));
    }

    @Test
    void refreshesActiveTargetEveryTick() {
        AidIntentState state = new AidIntentState();
        state.update(FIRST);
        assertEquals(List.of(new AidIntentState.Update(FIRST, true)), state.update(FIRST));
    }

    @Test
    void switchesTargetsInOrder() {
        AidIntentState state = new AidIntentState();
        state.update(FIRST);
        assertEquals(List.of(
                new AidIntentState.Update(FIRST, false),
                new AidIntentState.Update(SECOND, true)
        ), state.update(SECOND));
    }

    @Test
    void releaseAimLossOrHelperDownedStopsOnce() {
        AidIntentState state = new AidIntentState();
        state.update(FIRST);
        assertEquals(List.of(new AidIntentState.Update(FIRST, false)), state.update(null));
        assertEquals(List.of(), state.update(null));
    }

    @Test
    void reacquiresAfterInterruption() {
        AidIntentState state = new AidIntentState();
        state.update(FIRST);
        state.update(null);
        assertEquals(List.of(new AidIntentState.Update(FIRST, true)), state.update(FIRST));
    }

    @Test
    void resetClearsTargetWithoutNetworkUpdate() {
        AidIntentState state = new AidIntentState();
        state.update(FIRST);
        state.reset();
        assertNull(state.target());
        assertEquals(List.of(), state.update(null));
    }
}
