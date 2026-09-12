package com.bettercontent.downedplayerrevival.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BodyLayoutTest {
    @Test void minimumAndIntermediateHeightsKeepCureCopyClearOfButtonsAndFooter() {
        for (int height = 240; height <= 300; height++) {
            int spacing = BodyLayout.cureRowHeight(height);
            for (int row = 0; row < 3; row++) {
                int top = BodyLayout.CURE_TOP + row * spacing;
                int info = top + BodyLayout.CURE_INFO_OFFSET;
                assertTrue(info >= top + 19, "medicine detail must be below its button");
                assertTrue(info + 9 < height - 32, "complete detail must be above footer");
                if (row < 2) assertTrue(info + 9 <= top + spacing + 1, "detail must not touch next injury label");
            }
        }
    }
}
