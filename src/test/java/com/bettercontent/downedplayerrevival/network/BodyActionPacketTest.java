package com.bettercontent.downedplayerrevival.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BodyActionPacketTest {
    private static final UUID SUBJECT = UUID.randomUUID();

    @Test void openingAndStartingAreDistinctValidActions() {
        assertTrue(BodyActionPacket.overview(SUBJECT).validShape());
        assertTrue(new BodyActionPacket(SUBJECT, BodyActionPacket.START_TREATMENT, 0, 0, 0, false).validShape());
    }

    @Test void malformedStartCannotCarryAnUnrelatedSelection() {
        assertFalse(new BodyActionPacket(SUBJECT, BodyActionPacket.START_TREATMENT, 4, 99, 7, true).validShape());
        assertFalse(new BodyActionPacket(SUBJECT, 99, 0, 0, 0, false).validShape());
    }

    @Test void regionPriorityEditRequiresOnlyARealRegionAndNoPagingFields() {
        assertTrue(new BodyActionPacket(SUBJECT, BodyActionPacket.PROMOTE_REGION, 0, 0, 0, false).validShape());
        assertFalse(new BodyActionPacket(SUBJECT, BodyActionPacket.PROMOTE_REGION, 0, 1, 0, false).validShape());
        assertFalse(new BodyActionPacket(SUBJECT, BodyActionPacket.PROMOTE_REGION, -1, 0, 0, false).validShape());
    }
}
