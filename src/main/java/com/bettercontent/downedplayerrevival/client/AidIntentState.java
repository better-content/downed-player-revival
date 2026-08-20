package com.bettercontent.downedplayerrevival.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Client-side target transitions plus the active heartbeat used to recover transient server rejection. */
final class AidIntentState {
    private UUID target;

    List<Update> update(UUID desiredTarget) {
        List<Update> updates = new ArrayList<>(2);
        if (target != null && !target.equals(desiredTarget)) {
            updates.add(new Update(target, false));
        }
        target = desiredTarget;
        if (target != null) {
            updates.add(new Update(target, true));
        }
        return updates;
    }

    UUID target() {
        return target;
    }

    void reset() {
        target = null;
    }

    record Update(UUID targetId, boolean active) {}
}
