package com.bettercontent.downedplayerrevival.client;

import com.bettercontent.downedplayerrevival.RevivalConfig;
import com.bettercontent.downedplayerrevival.RevivalMod;
import com.bettercontent.downedplayerrevival.network.StateSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RevivalMod.MOD_ID, value = Dist.CLIENT)
public final class RevivalHud {
    private static final int WHITE = 0xFFF4F4F4;
    private static final int MUTED = 0xFFB7AAA8;
    private static final int RED = 0xFF9E3434;

    private RevivalHud() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        StateSyncPacket local = ClientRevivalState.get(minecraft.player.getUUID()).orElse(null);
        if (local != null) renderDowned(event.getGuiGraphics(), minecraft, local);
        else renderAid(event.getGuiGraphics(), minecraft);
    }

    private static void renderDowned(GuiGraphics graphics, Minecraft minecraft, StateSyncPacket state) {
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        graphics.fill(0, 0, width, 10, 0x48000000);
        graphics.fill(0, height - 10, width, height, 0x48000000);
        graphics.fill(0, 0, 10, height, 0x38000000);
        graphics.fill(width - 10, 0, width, height, 0x38000000);

        int center = width / 2;
        int y = height - 116;
        graphics.fill(center - 86, y - 6, center + 86, y + 40, 0xA8101010);
        graphics.fill(center - 85, y - 5, center + 85, y + 39, 0x70271212);
        graphics.drawCenteredString(minecraft.font, Component.translatable("downed_player_revival.hud.downed"), center, y, RED);
        graphics.drawCenteredString(minecraft.font,
                Component.translatable("downed_player_revival.hud.time", formatTicks(state.ticksLeft())), center, y + 12, WHITE);

        int unlock = RevivalConfig.GIVE_UP_UNLOCK_TICKS.get();
        if (state.downedTicks() < unlock) {
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("downed_player_revival.hud.give_up_locked", formatTicks(unlock - state.downedTicks())), center, y + 25, MUTED);
        } else if (state.giveUpTicks() > 0) {
            drawBar(graphics, center - 70, y + 26, 140,
                    state.giveUpTicks() / (float) RevivalConfig.GIVE_UP_HOLD_TICKS.get(), 0xFF742929);
            graphics.drawCenteredString(minecraft.font, Component.translatable("downed_player_revival.hud.giving_up"), center, y + 28, WHITE);
        } else {
            String key = RevivalConfig.GIVE_UP_HOLD_TICKS.get() <= 1
                    ? "downed_player_revival.hud.give_up_instant"
                    : "downed_player_revival.hud.give_up";
            graphics.drawCenteredString(minecraft.font, Component.translatable(key), center, y + 25, MUTED);
        }
    }

    private static void renderAid(GuiGraphics graphics, Minecraft minecraft) {
        UUID targetId = ClientRevivalInput.aidTarget();
        if (targetId == null) return;
        StateSyncPacket state = ClientRevivalState.get(targetId).orElse(null);
        Player target = minecraft.level == null ? null : minecraft.level.getPlayerByUUID(targetId);
        if (state == null || target == null) return;
        int center = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 22;
        drawBar(graphics, center - 55, y, 110,
                state.reviveProgress() / RevivalConfig.REVIVE_TICKS.get().floatValue(), 0xFFB7B2A1);
        graphics.drawCenteredString(minecraft.font,
                Component.translatable("downed_player_revival.hud.reviving", target.getDisplayName()), center, y + 4, WHITE);
        if (state.helperCount() > 1) {
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("downed_player_revival.hud.helpers", state.helperCount()), center, y + 16, MUTED);
        }
    }

    private static void drawBar(GuiGraphics graphics, int x, int y, int width, float progress, int color) {
        float bounded = Math.max(0.0f, Math.min(1.0f, progress));
        graphics.fill(x - 1, y - 1, x + width + 1, y + 10, 0xC0101010);
        graphics.fill(x, y, x + Math.round(width * bounded), y + 9, color);
    }

    private static String formatTicks(int ticks) {
        int seconds = Math.max(0, (ticks + 19) / 20);
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }
}
