package com.bettercontent.downedplayerrevival.client;
import com.bettercontent.downedplayerrevival.network.*;
import com.bettercontent.downedplayerrevival.state.Region;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.sounds.SoundEvents;
import java.util.*;
public final class ClientRevivalState {
 private static final Map<UUID,BodyView> STATES=new HashMap<>();
 private static BodyView recap;private static boolean recapWasDead;private static int captureDelay=-1;private static String captureLabel;
 public static Optional<BodyView> get(UUID id){return Optional.ofNullable(STATES.get(id));}
 public static BodyView recap(){return recap;}
 public static void accept(StateSyncPacket packet){
  var mc=Minecraft.getInstance();var body=packet.view();
  if(packet.mode()==2){recap=body;recapWasDead=false;return;}
  var previous=STATES.put(body.playerId(),body);
  if(previous!=null&&body.regions().stream().mapToInt(BodyView.RegionView::total).sum()>previous.regions().stream().mapToInt(BodyView.RegionView::total).sum()&&InjuryClientConfig.SOUND.get()&&mc.player!=null)mc.player.playSound(SoundEvents.ANVIL_LAND,.22f,.7f);
  if(packet.mode()==1){if(mc.screen instanceof BodyScreen screen&&screen.body().playerId().equals(body.playerId())){screen.update(body);screen.applySelection(Region.values()[Math.floorMod(packet.region(),6)],packet.history());}else mc.setScreen(new BodyScreen(body,Region.values()[Math.floorMod(packet.region(),6)],packet.history()));}
  if(packet.mode()==3&&mc.screen instanceof BodyScreen screen&&screen.body().playerId().equals(body.playerId()))screen.update(body);
 }
 public static void control(UiControlPacket packet){var mc=Minecraft.getInstance();switch(packet.operation()){
  case "own-body"->ClientRevivalInput.openOwnBody();
  case "body-view"->{if(mc.screen instanceof BodyScreen screen)screen.showPage(packet.text(),packet.delay());}
  case "presentation"->{InjuryClientConfig.REDUCED_MOTION.set(packet.progress()>0);InjuryClientConfig.SOUND.set(Boolean.parseBoolean(packet.text()));}
  case "inventory"->{if(mc.player!=null)mc.setScreen(new InventoryScreen(mc.player));}
  case "capture"->{captureLabel=packet.text().replaceAll("[^a-zA-Z0-9_-]","_");captureDelay=Math.max(2,Math.min(200,packet.delay()));}
  case "treatment"->{if(mc.screen instanceof BodyScreen screen)screen.treatment(packet.progress(),packet.text());}
  case "close"->{if(!(mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen))mc.setScreen(null);if(mc.player!=null&&!packet.text().isBlank())mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(packet.text()),true);}
  case "death-recap"->{if(mc.player!=null&&!mc.player.isAlive()&&!(mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen))mc.setScreen(new net.minecraft.client.gui.screens.DeathScreen(null,mc.level.getLevelData().isHardcore()));}
  default->{}
 }}
 public static void tick(Minecraft mc){if(mc.player==null){STATES.clear();return;}if(recap!=null&&!mc.player.isAlive())recapWasDead=true;if(recapWasDead&&mc.player.isAlive()){recap=null;recapWasDead=false;}if(captureDelay>=0&&--captureDelay==0){Screenshot.grab(mc.gameDirectory,captureLabel+".png",mc.getMainRenderTarget(),message->System.out.println("INJURY_GUI_CAPTURE "+message.getString()));captureDelay=-1;}}
 public static void clear(){STATES.clear();recap=null;recapWasDead=false;captureDelay=-1;captureLabel=null;RevivalHud.reset();}
}
