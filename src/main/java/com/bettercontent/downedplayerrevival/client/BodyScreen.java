package com.bettercontent.downedplayerrevival.client;

import com.bettercontent.downedplayerrevival.InjuryItems;
import com.bettercontent.downedplayerrevival.network.*;
import com.bettercontent.downedplayerrevival.state.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Production treatment screen. Operator selection uses the same view and action paths. */
public final class BodyScreen extends Screen {
    public static final int PAPER = 0xF0181D23, INK = 0xFFF0E9DC, MUTED = 0xFFA5B2B9;
    public static final int RED = 0xFFED9E91, GREEN = 0xFFA8CEB2;
    private BodyView body;
    private Region selected;
    private boolean history;
    private float progress;
    private String status = "";
    private int left, right, detail, panelHeight, offsetY;
    private final List<Button> treatments = new ArrayList<>();
    private final Map<Region, Button> regions = new EnumMap<>(Region.class);
    private Button done;

    public BodyScreen(BodyView body, Region selected, boolean history) {
        super(Component.literal("Body & treatment"));
        this.body = body;
        this.selected = selected;
        this.history = history;
    }

    public BodyView body() { return body; }
    public void update(BodyView body) { this.body = body; updateButtons(); }
    public void treatment(float progress, String message) {
        this.progress = progress >= 1 ? 0 : progress;
        status = message;
        updateButtons();
    }
    public void applySelection(Region region, boolean history) {
        if (selected != region || this.history != history) {
            selected = region;
            this.history = history;
            rebuildWidgets();
        }
    }
    public void select(Region region, boolean history, int page) {
        selected = region;
        this.history = history;
        RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(), 0, region.ordinal(), 0, page, history));
        rebuildWidgets();
    }

    @Override protected void init() {
        left = Math.max(8, (width - 540) / 2);
        right = width - left;
        panelHeight = Math.min(height, 300);
        offsetY = (height - panelHeight) / 2;
        detail = left + Math.max(110, Math.min(154, (right - left) / 3));
        done = addButton("Done", right - 53, 15, 43, 18, b -> onClose());
        regions.clear();
        int rowHeight = regionHeight();
        for (Region region : Region.values()) {
            var button = new RegionButton(left + 37, offsetY + 92 + region.ordinal() * rowHeight,
                detail - left - 44, rowHeight - 2, region, b -> select(region, false, 0));
            regions.put(region, addRenderableWidget(button));
        }
        int tabWidth = (right - detail - 13) / 2;
        addButton("Active injuries", detail, 68, tabWidth, 19, b -> select(selected, false, 0));
        addButton("History", detail + tabWidth + 3, 68, tabWidth, 19, b -> select(selected, true, 0));
        treatments.clear();
        if (!history) {
            for (MaimType type : MaimType.values()) {
                treatments.add(addButton("Apply " + InjuryItems.cureName(type), right - 111,
                    BodyLayout.CURE_TOP + type.ordinal() * treatmentRowHeight(), 101, 19,
                    b -> RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(), 1,
                        selected.ordinal(), type.ordinal(), 0, false))));
            }
        } else {
            addButton("Previous", detail, panelHeight - 54, 65, 18,
                b -> select(selected, true, Math.max(0, body.historyPage() - 1))).active = body.historyPage() > 0;
            addButton("Next", right - 63, panelHeight - 54, 53, 18,
                b -> select(selected, true, body.historyPage() + 1)).active = body.historyPage() + 1 < body.historyPages();
        }
        updateButtons();
    }

    private Button addButton(String label, int x, int y, int w, int h, Button.OnPress press) {
        return addRenderableWidget(Button.builder(Component.literal(label), press).bounds(x, y + offsetY, w, h).build());
    }
    private int regionHeight() { return Math.max(18, Math.min(28, (panelHeight - 130) / 6)); }
    private int treatmentRowHeight() { return BodyLayout.cureRowHeight(panelHeight); }
    private void updateButtons() {
        for (int i = 0; i < treatments.size(); i++) {
            treatments.get(i).active = body.region(selected).count(MaimType.values()[i]) > 0
                && body.supplies().get(i) > 0 && progress <= 0;
        }
        regions.forEach((r, button) -> button.setMessage(Component.literal(BodyView.label(r) + " " + body.region(r).total())));
        if (done != null) done.setMessage(Component.literal(progress > 0 ? "Cancel" : "Done"));
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        g.pose().pushPose();
        g.pose().translate(0, offsetY, 0);
        g.fill(left, 8, right, panelHeight - 8, PAPER);
        g.fill(left, 8, left + 3, panelHeight - 8, body.atDoor() ? RED : GREEN);
        boolean self = minecraft.player != null && minecraft.player.getUUID().equals(body.playerId());
        text(g, self ? "YOUR BODY" : "TREATING " + body.name(), left + 12, 17, INK);
        String hp = body.atDoor() ? "0 HP · DEATH'S DOOR" : String.format(Locale.ROOT, "%.1f/%.1f HP", body.health(), body.maxHealth());
        if (body.atDoor()) hp += body.healingLockTicks() > 0
            ? String.format(Locale.ROOT, " · Heal in %.1fs", body.healingLockTicks() / 20.0) : " · Healing available";
        text(g, hp, left + 12, 33, body.atDoor() ? RED : MUTED);
        text(g, "At 0 HP, each hit kills or maims. Injuries add risk.", left + 12, 45, MUTED);
        text(g, "Trauma " + body.trauma() + " · effects " + Math.round(body.multiplier()*100) + "% · " + body.traumaLifetimeLabel() + " per hit", left + 12, 55, MUTED);
        text(g, "Body · active count", left + 9, 73, MUTED);
        renderBody(g);
        if (history) renderHistory(g); else renderActive(g);
        g.fill(left + 10, panelHeight - 32, right - 10, panelHeight - 31, 0xFF39434A);
        if (progress > 0) {
            g.fill(left + 10, panelHeight - 29, right - 10, panelHeight - 27, 0xFF39434A);
            g.fill(left + 10, panelHeight - 29, left + 10 + (int)((right-left-20) * progress), panelHeight - 27, GREEN);
        }
        String footer = status.isEmpty() ? (history ? "Treatment history lasts for this life." : (self ? "Damage interrupts. Keep this screen open." : "Stay close. Keep open. Damage interrupts.")) : status;
        if (progress > 0) footer += " · " + Math.round(progress * 100) + "%";
        text(g, footer, left + 12, panelHeight - 23, progress > 0 ? GREEN : MUTED);
        g.pose().popPose();
        super.render(g, mouseX, mouseY, partial);
    }

    private void renderBody(GuiGraphics g) {
        int x = left + 7, y = 110;
        bodyPart(g, Region.HEAD, x + 8, y, 12, 12);
        bodyPart(g, Region.TORSO, x + 7, y + 15, 14, 27);
        bodyPart(g, Region.LEFT_ARM, x, y + 15, 5, 30);
        bodyPart(g, Region.RIGHT_ARM, x + 23, y + 15, 5, 30);
        bodyPart(g, Region.LEFT_LEG, x + 7, y + 44, 6, 33);
        bodyPart(g, Region.RIGHT_LEG, x + 15, y + 44, 6, 33);
    }
    private void bodyPart(GuiGraphics g, Region region, int x, int y, int w, int h) {
        var r = body.region(region);
        int border = selected == region ? INK : 0xFF55636B;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        int color = r.total() == 0 ? 0xFF4F6960 : r.total() < 3 ? 0xFFA66F61 : 0xFFC58673;
        g.fill(x, y, x + w, y + h, color);
        if (r.treated() > 0) { g.fill(x, y + h / 2, x + w, y + h / 2 + 3, GREEN); }
    }

    private void renderActive(GuiGraphics g) {
        text(g, BodyView.label(selected) + " · " + body.region(selected).total() + " active", detail, 93, INK);
        String effect = switch (selected) {
            case HEAD -> "Extra death risk: +";
            case TORSO -> "Maximum HP: -";
            case LEFT_LEG, RIGHT_LEG -> "Movement (this leg): -";
            case LEFT_ARM, RIGHT_ARM -> "Both arms: melee & reach -";
        };
        text(g, effect + Math.round(body.region(selected).reduction() * 100) + "%", detail, 105, MUTED);
        for (MaimType type : MaimType.values()) {
            int y = BodyLayout.CURE_TOP + type.ordinal() * treatmentRowHeight();
            int n = body.region(selected).count(type), available = body.supplies().get(type.ordinal());
            text(g, BodyView.label(type) + " ×" + n, detail, y + 1, n > 0 ? RED : MUTED);
            String info = n == 0 ? "No injury to cure" : available == 0
                ? "Need 1 " + InjuryItems.cureName(type) + " · you have 0 · " + String.format(Locale.ROOT, "%.1fs", body.treatmentSeconds())
                : "You: " + available + " · uses 1 · " + String.format(Locale.ROOT, "%.1fs", body.treatmentSeconds());
            text(g, info, detail, y + BodyLayout.CURE_INFO_OFFSET, available == 0 && n > 0 ? RED : MUTED);
        }
    }

    private void renderHistory(GuiGraphics g) {
        text(g, BodyView.label(selected) + " · " + body.region(selected).treated() + " treatments", detail, 93, INK);
        if (body.history().isEmpty()) {
            text(g, "No treatments applied here yet.", detail, 109, MUTED);
            text(g, "Cure an injury to add its record.", detail, 123, MUTED);
        } else {
            for (int i = 0; i < body.history().size(); i++) {
                var entry = body.history().get(i);
                var id = net.minecraft.resources.ResourceLocation.tryParse(entry.itemId());
                var item = id == null ? null : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id);
                String name = item == null || item == net.minecraft.world.item.Items.AIR ? entry.itemId() : item.getDescription().getString();
                String line = entry.index() + ". " + name + " · cured " + BodyView.label(entry.type());
                float scale = Math.min(1, (right - detail - 10f) / font.width(line));
                g.pose().pushPose(); g.pose().translate(detail, 110 + i * 11, 0); g.pose().scale(scale, scale, 1);
                text(g, line, 0, 0, GREEN); g.pose().popPose();
            }
        }
        text(g, (body.historyPage() + 1) + " / " + body.historyPages(), detail + 69, panelHeight - 48, MUTED);
    }

    private void text(GuiGraphics g, String value, int x, int y, int color) {
        // Essential copy is laid out to fit at Minecraft's minimum GUI size, never silently elided.
        g.drawString(font, value, x, y, color, false);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void removed() {
        if (minecraft.getConnection() != null) RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(), 2, 0, 0, 0, false));
        super.removed();
    }

    private final class RegionButton extends Button {
        private final Region region;
        RegionButton(int x, int y, int width, int height, Region region, OnPress press) {
            super(x, y, width, height, Component.empty(), press, DEFAULT_NARRATION);
            this.region = region;
        }
        @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
            int border = selected == region ? GREEN : isHoveredOrFocused() ? INK : 0xFF566168;
            g.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), border);
            g.fill(getX()+1, getY()+1, getX()+getWidth()-1, getY()+getHeight()-1, 0xFF252D34);
            float scale = Math.min(1, (getWidth()-6f) / font.width(getMessage()));
            g.pose().pushPose();
            g.pose().translate(getX()+3, getY()+(getHeight()-8*scale)/2, 0);
            g.pose().scale(scale, scale, 1);
            g.drawString(font, getMessage(), 0, 0, body.region(region).total()>0 ? RED : GREEN, false);
            g.pose().popPose();
        }
    }
}
