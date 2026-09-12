package com.bettercontent.downedplayerrevival.state;

public enum MaimType {
    CRACKED("minecraft:stick"), BURNT("downed_player_revival:balm"), OPENED("downed_player_revival:soocher");
    private final String defaultTreatment;
    MaimType(String defaultTreatment) { this.defaultTreatment = defaultTreatment; }
    public String defaultTreatment() { return defaultTreatment; }
    public String translationKey() { return "maim.downed_player_revival." + name().toLowerCase(java.util.Locale.ROOT); }
}
