package com.bettercontent.revival.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RevivalRulesTest {
    @Test
    void retainsKnownCompatibilityBypasses() {
        assertTrue(RevivalRules.bypassesDownedState("gorgon"));
        assertTrue(RevivalRules.bypassesDownedState("death.attack.sgcraft:iris"));
        assertTrue(RevivalRules.bypassesDownedState("minecraft:generic_kill"));
        assertFalse(RevivalRules.bypassesDownedState("minecraft:mob_attack"));
    }
}
