package com.bettercontent.downedplayerrevival.state;

public enum Region {
    HEAD, TORSO, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG;
    public String translationKey() { return "region.downed_player_revival." + name().toLowerCase(java.util.Locale.ROOT); }
}
