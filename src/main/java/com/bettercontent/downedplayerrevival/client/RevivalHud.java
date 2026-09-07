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

import java.util.UUID;

@Mod.EventBusSubscriber(modid = RevivalMod.MOD_ID, value = Dist.CLIENT)
public final class RevivalHud {
    private static final int WHITE = 0xFFF4F4F4;
    private static final int MUTED = 0xFFB7AAA8;
    private static final int RED = 0xFFE66A70;
    private static final int CRITICAL = 0xFFFFD166;
    private static final int FRAME_DARK = 0xD018090B;
    private static final int FRAME_RED = 0xD09E2932;
    private static final int PANEL = 0xE8120E10;
    private static final int PANEL_INNER = 0xD02A1417;

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
        RevivalHudPresentation.View view = RevivalHudPresentation.resolve(
                state.ticksLeft(),
                state.downedTicks(),
                state.reviveProgress(),
                state.helperCount(),
                state.giveUpTicks(),
                RevivalConfig.REVIVE_TICKS.get(),
                RevivalConfig.GIVE_UP_UNLOCK_TICKS.get(),
                RevivalConfig.GIVE_UP_HOLD_TICKS.get()
        );

        drawEdgeFrame(graphics, width, height, view.banner() == RevivalHudPresentation.BannerState.CRITICAL);

        int center = width / 2;
        int bannerWidth = Math.min(300, Math.max(120, width - 24));
        int bannerLeft = center - bannerWidth / 2;
        int bannerRight = bannerLeft + bannerWidth;
        graphics.fill(bannerLeft - 2, 9, bannerRight + 2, 51, FRAME_DARK);
        graphics.fill(bannerLeft, 11, bannerRight, 49, PANEL_INNER);
        graphics.fill(bannerLeft, 11, bannerRight, 14,
                view.banner() == RevivalHudPresentation.BannerState.CRITICAL ? CRITICAL : FRAME_RED);
        graphics.drawCenteredString(minecraft.font,
                Component.translatable("downed_player_revival.hud.you_are_downed"), center, 18, WHITE);
        graphics.drawCenteredString(minecraft.font, Component.translatable(bannerSubtitle(view)), center, 29,
                view.banner() == RevivalHudPresentation.BannerState.CRITICAL ? CRITICAL : RED);
        graphics.drawCenteredString(minecraft.font,
                Component.translatable("downed_player_revival.hud.time",
                        RevivalHudPresentation.formatTicks(view.ticksLeft())), center, 40, WHITE);

        int panelWidth = Math.min(272, Math.max(120, width - 24));
        int panelHeight = view.reviving() ? 68 : 50;
        int panelLeft = center - panelWidth / 2;
        int panelTop = Math.max(58, height - panelHeight - 42);
        graphics.fill(panelLeft - 2, panelTop - 2, panelLeft + panelWidth + 2, panelTop + panelHeight + 2, PANEL);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, PANEL_INNER);

        int contentY = panelTop + 7;
        if (view.reviving()) {
            int barWidth = Math.max(80, panelWidth - 28);
            drawBar(graphics, center - barWidth / 2, contentY, barWidth, view.reviveProgress(), 0xFF7CC6A6);
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("downed_player_revival.hud.revive_progress",
                            Math.round(view.reviveProgress() * 100.0f)), center, contentY + 1, WHITE);
            if (view.helperCount() > 1) {
                graphics.drawCenteredString(minecraft.font,
                        Component.translatable("downed_player_revival.hud.helpers", view.helperCount()), center,
                        contentY + 13, MUTED);
            } else {
                graphics.drawCenteredString(minecraft.font,
                        Component.translatable("downed_player_revival.hud.one_helper"), center, contentY + 13, MUTED);
            }
            contentY += 30;
        } else {
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("downed_player_revival.hud.wait_for_help"), center, contentY, WHITE);
            contentY += 17;
        }

        if (view.giveUp() == RevivalHudPresentation.GiveUpState.LOCKED) {
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("downed_player_revival.hud.give_up_locked",
                            RevivalHudPresentation.formatTicks(view.giveUpLockedTicks())), center, contentY, MUTED);
        } else if (view.giveUp() == RevivalHudPresentation.GiveUpState.HOLDING) {
            int barWidth = Math.max(80, panelWidth - 28);
            drawBar(graphics, center - barWidth / 2, contentY - 2, barWidth, view.giveUpProgress(), 0xFF9E343C);
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("downed_player_revival.hud.giving_up"), center, contentY - 1, WHITE);
        } else {
            String key = RevivalConfig.GIVE_UP_HOLD_TICKS.get() <= 1
                    ? "downed_player_revival.hud.give_up_instant"
                    : "downed_player_revival.hud.give_up";
            graphics.drawCenteredString(minecraft.font, Component.translatable(key), center, contentY, MUTED);
        }
    }

    private static String bannerSubtitle(RevivalHudPresentation.View view) {
        return switch (view.banner()) {
            case DOWNED -> "downed_player_revival.hud.wait_for_help";
            case REVIVING -> "downed_player_revival.hud.revival_in_progress";
            case CRITICAL -> "downed_player_revival.hud.bleeding_out";
        };
    }

    private static void drawEdgeFrame(GuiGraphics graphics, int width, int height, boolean critical) {
        int accent = critical ? CRITICAL : FRAME_RED;
        graphics.fill(0, 0, width, 8, FRAME_DARK);
        graphics.fill(0, height - 8, width, height, FRAME_DARK);
        graphics.fill(0, 0, 8, height, FRAME_DARK);
        graphics.fill(width - 8, 0, width, height, FRAME_DARK);
        graphics.fill(0, 0, width, 3, accent);
        graphics.fill(0, height - 3, width, height, accent);
        graphics.fill(0, 0, 3, height, accent);
        graphics.fill(width - 3, 0, width, height, accent);
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

}
