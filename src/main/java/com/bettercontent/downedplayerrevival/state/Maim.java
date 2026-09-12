package com.bettercontent.downedplayerrevival.state;

import java.util.Objects;

public record Maim(long id, Region region, MaimType type, long tick) {
    public Maim { Objects.requireNonNull(region); Objects.requireNonNull(type); }
}
