package com.bettercontent.downedplayerrevival.client;
/** Treatment geometry reserves complete text lines even at Minecraft's minimum GUI height. */
public final class BodyLayout {
    public static final int CURE_TOP = 116;
    public static final int CURE_INFO_OFFSET = 20;
    public static int cureRowHeight(int panelHeight) {
        return Math.max(30, Math.min(42, (panelHeight - 150) / 3));
    }
    private BodyLayout() {}
}
