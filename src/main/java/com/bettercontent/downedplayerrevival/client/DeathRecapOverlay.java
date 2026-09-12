package com.bettercontent.downedplayerrevival.client;
import com.bettercontent.downedplayerrevival.RevivalMod;
import com.bettercontent.downedplayerrevival.network.BodyView;
import com.bettercontent.downedplayerrevival.state.Region;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Native death controls retain their handlers; the bodily record uses the space above them. */
@Mod.EventBusSubscriber(modid=RevivalMod.MOD_ID,value=Dist.CLIENT)
public final class DeathRecapOverlay {
 public record Bounds(int x,int y,int width,int height){}
 private static Screen previous;private static long opened;
 public static Bounds reservedBounds(int width,int height){return new Bounds(Math.max(8,(width-560)/2),110,Math.min(560,width-16),Math.max(0,height-140));}
 @SubscribeEvent public static void init(ScreenEvent.Init.Post event){if(!(event.getScreen() instanceof DeathScreen screen))return;var buttons=event.getListenersList().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();if(buttons.size()==2){int buttonWidth=Math.min(200,(screen.width-28)/2);for(int i=0;i<2;i++){var button=buttons.get(i);button.setWidth(buttonWidth);button.setX(screen.width/2+(i==0?-buttonWidth-3:3));button.setY(screen.height-24);}}}
 @SubscribeEvent public static void render(ScreenEvent.Render.Post event){if(!(event.getScreen() instanceof DeathScreen screen))return;BodyView body=ClientRevivalState.recap();if(body==null)return;var mc=Minecraft.getInstance();if(previous!=screen){previous=screen;opened=System.currentTimeMillis();if(InjuryClientConfig.SOUND.get()&&mc.player!=null)mc.player.playSound(SoundEvents.BASALT_BREAK,.35f,.65f);}
  var box=reservedBounds(screen.width,screen.height);var g=event.getGuiGraphics();if(box.height()<80)return;g.fill(box.x(),box.y(),box.x()+box.width(),box.y()+box.height(),0xDA181D23);
  g.drawCenteredString(mc.font,"BODY RECORD · Cracked / Burnt / Opened",box.x()+box.width()/2,box.y()+5,BodyScreen.INK);
  int columns=box.width()>=440?3:2,rows=(6+columns-1)/columns,cellW=(box.width()-16)/columns,cellH=(box.height()-19)/rows;
  for(var region:Region.values()){int i=region.ordinal();if(!InjuryClientConfig.REDUCED_MOTION.get()&&System.currentTimeMillis()-opened<i*80L)continue;int x=box.x()+8+(i%columns)*cellW,y=box.y()+19+(i/columns)*cellH;var r=body.region(region);
   g.drawString(mc.font,BodyView.label(region),x,y,r.total()>0?BodyScreen.RED:BodyScreen.GREEN,false);
   // Full type names remain in the shared header; each column is always in that order.
   g.drawString(mc.font,"Active "+r.cracked()+" / "+r.burnt()+" / "+r.opened(),x,y+9,BodyScreen.RED,false);
   g.drawString(mc.font,"Treated "+r.treatedCracked()+" / "+r.treatedBurnt()+" / "+r.treatedOpened(),x,y+18,BodyScreen.GREEN,false);
  }
 }
}
