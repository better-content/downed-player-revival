package com.bettercontent.downedplayerrevival.client;

import com.bettercontent.downedplayerrevival.network.*;
import com.bettercontent.downedplayerrevival.state.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

/** A focused treatment view, with separate region selection and explanations. */
public final class BodyScreen extends Screen {
    public static final int PAPER = 0xF0181D23, INK = 0xFFF0E9DC, MUTED = 0xFFA5B2B9;
    public static final int RED = 0xFFED9E91, GREEN = 0xFFA8CEB2;
    private BodyView body;
    private Region selected;
    private boolean history, choosingRegion, help;
    private float progress;
    private String status = "", helpReturn = "detail";
    private int statusTicks;
    private int left, top, panelWidth, panelHeight, scroll;
    private final Map<Region, Button> regions = new EnumMap<>(Region.class);
    private Button done, start, previousHistory, nextHistory;

    public BodyScreen(BodyView body, Region selected, boolean history) {
        super(Component.literal("Body & treatment"));
        this.body = body; this.selected = selected; this.history = history;
    }
    public BodyView body() { return body; }
    public void update(BodyView next) {
        boolean changed = !activeTypes().equals(Arrays.stream(MaimType.values()).filter(t -> next.region(selected).count(t) > 0).toList())
            || !body.treatmentPriority().equals(next.treatmentPriority()) || body.treatmentActive() != next.treatmentActive();
        body = next;
        int previousScroll = scroll; scroll = Math.min(scroll, maxScroll());
        if (changed || scroll != previousScroll) { rebuildWidgets(); }
        else updateButtons();
    }
    public void treatment(float value, String message) {
        progress = value >= 1 ? 0 : value; status = message; statusTicks = 100; updateButtons();
    }
    public void applySelection(Region region, boolean history) {
        if (selected != region || this.history != history || choosingRegion || help) {
            selected = region; this.history = history; choosingRegion = help = false; scroll = 0; rebuildWidgets();
        }
    }
    private void select(Region region, boolean history, int page) {
        selected = region; this.history = history; choosingRegion = help = false; scroll = 0;
        if (progress <= 0) status = "";
        RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(), 0, region.ordinal(), 0, page, history));
        rebuildWidgets();
    }
    /** Console controls use the same navigation as the visible buttons. */
    public void showPage(String page, int offset) {
        if (page.equals("help") && !help) helpReturn = choosingRegion ? "regions" : "detail";
        choosingRegion = page.equals("regions"); help = page.equals("help");
        if (progress <= 0) status = "";
        scroll = Math.max(0, Math.min(offset, maxScroll())); rebuildWidgets();
    }
    @Override protected void init() {
        panelWidth = Math.min(500, width - 24); panelHeight = Math.min(390, height - 16);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        scroll = Math.min(scroll, maxScroll());
        regions.clear(); previousHistory = nextHistory = null;
        done = button("Done", panelWidth - 58, 12, 44, 20, b -> onClose());
        button(help ? "Back" : "Help", panelWidth - 108, 12, 44, 20, b -> showPage(help ? helpReturn : "help", 0));
        start = button(body.treatmentActive() ? "Running" : "Start", panelWidth - 170, 12, 56, 20,
            b -> RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(), 1, 0, 0, 0, false)));
        if (help) { updateButtons(); return; }
        if (choosingRegion) {
            int buttonWidth = (panelWidth - 76) / 2;
            for (int i = 0; i < body.treatmentPriority().size(); i++) {
                Region region = body.treatmentPriority().get(i);
                int x = i % 2 == 0 ? 14 : panelWidth - 14 - buttonWidth;
                int y = 80 + (i / 2) * 36;
                regions.put(region, button((i + 1) + ". " + BodyView.label(region) + " (" + body.region(region).total() + ")",
                    x, y, buttonWidth - 30, 26, b -> select(region, false, 0)));
                button("↑", x + buttonWidth - 26, y, 26, 26,
                    b -> RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(), 4, region.ordinal(), 0, 0, false))).active = i > 0;
            }
            updateButtons(); return;
        }
        button("Region: " + BodyView.label(selected), 14, 55, panelWidth - 166, 22, b -> showPage("regions", 0));
        button("Injuries", panelWidth - 144, 55, 62, 22, b -> select(selected, false, 0)).active = history;
        button("History", panelWidth - 76, 55, 62, 22, b -> select(selected, true, 0)).active = !history;
        if (history) {
            previousHistory = button("Previous", 14, panelHeight - 32, 68, 20, b -> select(selected, true, body.historyPage() - 1));
            nextHistory = button("Next", panelWidth - 82, panelHeight - 32, 68, 20, b -> select(selected, true, body.historyPage() + 1));
        }
        updateButtons();
    }
    private Button button(String label, int x, int y, int w, int h, Button.OnPress press) {
        return addRenderableWidget(Button.builder(Component.literal(label), press).bounds(left + x, top + y, w, h).build());
    }
    private List<MaimType> activeTypes() { return Arrays.stream(MaimType.values()).filter(t -> body.region(selected).count(t) > 0).toList(); }
    private int contentTop() { return 106; }
    private int contentBottom() { return panelHeight - 48; }
    private int maxScroll() { return Math.max(0, (history ? body.history().size() * 28 : Math.max(0, activeTypes().size() * 76 - 10)) - (contentBottom() - contentTop())); }
    private void updateButtons() {
        regions.forEach((region, button) -> button.setMessage(Component.literal((body.treatmentPriority().indexOf(region) + 1) + ". " + BodyView.label(region) + " (" + body.region(region).total() + ")")));
        if (start != null) {
            start.active = !body.treatmentActive() && Arrays.stream(Region.values()).anyMatch(r -> body.region(r).total() > 0);
            start.setMessage(Component.literal(body.treatmentActive() ? "Running" : "Start"));
        }
        if (previousHistory != null) previousHistory.active = body.historyPage() > 0;
        if (nextHistory != null) nextHistory.active = body.historyPage() + 1 < body.historyPages();
        if (done != null) done.setMessage(Component.literal(body.treatmentActive() ? "Cancel" : "Done"));
    }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        if (!help && !choosingRegion && x >= left && x < left + panelWidth && y >= top + contentTop() && y < top + contentBottom()) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int)(delta * (history ? 28 : 76)))); rebuildWidgets(); return true;
        }
        return super.mouseScrolled(x, y, delta);
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        g.fill(left, top, left + panelWidth, top + panelHeight, PAPER);
        g.fill(left, top, left + 2, top + panelHeight, body.atDoor() ? RED : GREEN);
        boolean self = minecraft.player != null && minecraft.player.getUUID().equals(body.playerId());
        text(g, self ? "YOUR BODY" : "TREATING " + body.name(), 14, 15, INK);
        String health = body.atDoor() ? "0 HP · Death's Door" : String.format(Locale.ROOT, "%.1f / %.1f HP", body.health(), body.maxHealth());
        if (body.atDoor()) health += body.healingLockTicks() > 0 ? String.format(Locale.ROOT, " · heal in %.1fs", body.healingLockTicks()/20.) : " · heal to leave";
        text(g, health, 14, 37, body.atDoor() ? RED : MUTED);
        if (help) renderHelp(g);
        else if (choosingRegion) renderRegions(g);
        else {
            if (!history) text(g, effect(), 14, 88, MUTED);
            else text(g, body.region(selected).treated() + " applied treatments", 14, 88, MUTED);
            g.enableScissor(left + 12, top + contentTop(), left + panelWidth - 12, top + contentBottom());
            if (history) renderHistory(g); else renderInjuries(g);
            g.disableScissor();
            if (maxScroll() > 0) {
                text(g, "Scroll ↕", panelWidth - 62, 88, MUTED);
                int track = contentBottom() - contentTop(), thumb = Math.max(16, track * track / (track + maxScroll()));
                int y = top + contentTop() + (track-thumb) * scroll / maxScroll();
                g.fill(left+panelWidth-9,top+contentTop(),left+panelWidth-7,top+contentBottom(),0xFF39434A);
                g.fill(left+panelWidth-9,y,left+panelWidth-7,y+thumb,MUTED);
            }
            if (history) text(g, "Page " + (body.historyPage()+1) + " / " + body.historyPages(), (panelWidth-72)/2, panelHeight-26, MUTED);
        }
        int footer = panelHeight - 30;
        g.fill(left+14,top+footer-7,left+panelWidth-14,top+footer-6,0xFF39434A);
        String message = status.isEmpty() ? help ? "Treatment history lasts for this life." : choosingRegion ? "Set region priority, then press Start." : history ? "Cured injuries remain in this record." : (self ? "Press Start. Damage or closing interrupts care." : "Press Start. Stay close and keep the screen open.") : status;
        if (progress > 0) {
            g.fill(left+14,top+footer-7,left+14+(int)((panelWidth-28)*progress),top+footer-5,GREEN);
            message = "Applying treatment · " + Math.round(progress*100) + "%";
        }
        if (!history || help || choosingRegion) g.drawWordWrap(font,Component.literal(message),left+14,top+footer,panelWidth-28,progress>0?GREEN:MUTED);
        super.render(g,mouseX,mouseY,partial);
    }
    private String effect() {
        String label = switch(selected) {
            case HEAD -> "Extra death risk: +";
            case TORSO -> "Maximum HP: -";
            case LEFT_LEG, RIGHT_LEG -> "Movement from this leg: -";
            case LEFT_ARM, RIGHT_ARM -> "Both arms: melee & reach -";
        };
        return label + Math.round(body.region(selected).reduction()*100) + "%";
    }
    private void renderInjuries(GuiGraphics g) {
        List<MaimType> types = activeTypes();
        if (types.isEmpty()) {
            text(g,"No active injuries here",24,contentTop()+20,GREEN);
            text(g,"Choose another region to inspect your body.",24,contentTop()+40,MUTED); return;
        }
        for (int i=0;i<types.size();i++) {
            MaimType type=types.get(i); int y=contentTop()+i*76-scroll;
            if (y < contentTop() || y + 66 > contentBottom()) continue;
            g.fill(left+14,top+y,left+panelWidth-14,top+y+66,0xFF252D34);
            text(g,BodyView.label(type)+" ×"+body.region(selected).count(type),26,y+18,RED);
            text(g,"Queued care · no item required",26,y+40,MUTED);
            text(g,String.format(Locale.ROOT,"Treatment time: %.1f seconds",body.treatmentSeconds()),26,y+53,MUTED);
        }
    }
    private void renderHistory(GuiGraphics g) {
        if(body.history().isEmpty()){text(g,"No treatments applied here yet.",24,contentTop()+20,MUTED);return;}
        for(int i=0;i<body.history().size();i++) {
            var entry=body.history().get(i); var id=net.minecraft.resources.ResourceLocation.tryParse(entry.itemId());
            var item=id==null?null:net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id);
            String name=entry.itemId().equals("downed_player_revival:care")?"Care":item==null||item==net.minecraft.world.item.Items.AIR?entry.itemId():item.getDescription().getString();
            int y=contentTop()+i*28-scroll;
            if (y < contentTop() || y + 23 > contentBottom()) continue;
            text(g,entry.index()+". "+name+" applied",22,y+2,GREEN);
            text(g,"Cured "+BodyView.label(entry.type()).toLowerCase(Locale.ROOT),36,y+14,MUTED);
        }
    }
    private void renderRegions(GuiGraphics g) {
        text(g,"Choose a body region · active injury counts",14,59,MUTED);
        int x=panelWidth/2-14,y=89;
        part(g,Region.HEAD,x+8,y,12,12); part(g,Region.TORSO,x+7,y+15,14,25);
        part(g,Region.LEFT_ARM,x,y+15,5,28); part(g,Region.RIGHT_ARM,x+23,y+15,5,28);
        part(g,Region.LEFT_LEG,x+7,y+43,6,31); part(g,Region.RIGHT_LEG,x+15,y+43,6,31);
    }
    private void part(GuiGraphics g,Region region,int x,int y,int w,int h) {
        g.fill(left+x-1,top+y-1,left+x+w+1,top+y+h+1,region==selected?INK:0xFF55636B);
        g.fill(left+x,top+y,left+x+w,top+y+h,body.region(region).total()==0?0xFF4F6960:0xFFA66F61);
        if(body.region(region).treated()>0)g.fill(left+x,top+y+h/2,left+x+w,top+y+h/2+3,GREEN);
    }
    private void renderHelp(GuiGraphics g) {
        String[] lines={"At zero HP you enter Death's Door.","Every further hit kills or maims.","More active injuries mean more death risk.","Heal above zero to leave. Injuries remain.","Press Start for automatic ordered care.","Move regions up to change priority.","Recent hits intensify physical penalties.","Trauma: "+body.trauma()+" · effects "+Math.round(body.multiplier()*100)+"% · "+body.traumaLifetimeLabel()+" per hit"};
        for(int i=0;i<lines.length;i++)text(g,lines[i],14,61+i*15,i<4?INK:MUTED);
    }
    private void text(GuiGraphics g,String text,int x,int y,int color) { g.drawString(font,text,left+x,top+y,color,false); }
    @Override public void tick() { if (progress <= 0 && statusTicks > 0 && --statusTicks == 0) status = ""; }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void removed(){if(minecraft.getConnection()!=null)RevivalNetwork.CHANNEL.sendToServer(new BodyActionPacket(body.playerId(),2,0,0,0,false));super.removed();}
}
