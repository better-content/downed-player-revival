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
 private static Screen hintScreen;private static int hintHeight;
 /** Consumers reserve measured copy space; this class alone positions native death controls. */
 public static Bounds reserveHint(Screen screen,int measuredHeight){hintScreen=screen;hintHeight=Math.max(0,measuredHeight);int w=Math.min(560,screen.width-16);return new Bounds(Math.max(8,(screen.width-w)/2),108,w,hintHeight);}
 private static int hintOffset(int width,int height){return hintScreen!=null&&hintScreen.width==width&&hintScreen.height==height?hintHeight+4:0;}
 public static Bounds reservedBounds(int width,int height){return new Bounds(Math.max(8,(width-560)/2),Math.max(110,108+hintOffset(width,height)),Math.min(560,width-16),Math.max(0,height-30-Math.max(110,108+hintOffset(width,height))));}
 @SubscribeEvent public static void init(ScreenEvent.Init.Post event){if(!(event.getScreen() instanceof DeathScreen screen))return;hintScreen=null;hintHeight=0;var buttons=event.getListenersList().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();if(buttons.size()==2){int buttonWidth=Math.min(200,(screen.width-28)/2);for(int i=0;i<2;i++){var button=buttons.get(i);button.setWidth(buttonWidth);button.setX(screen.width/2+(i==0?-buttonWidth-3:3));button.setY(screen.height-24);}}}
 @SubscribeEvent public static void render(ScreenEvent.Render.Post event){if(!(event.getScreen() instanceof DeathScreen screen))return;BodyView body=ClientRevivalState.recap();if(body==null)return;var mc=Minecraft.getInstance();if(previous!=screen){previous=screen;opened=System.currentTimeMillis();if(InjuryClientConfig.SOUND.get()&&mc.player!=null)mc.player.playSound(SoundEvents.BASALT_BREAK,.35f,.65f);}
  var box=reservedBounds(screen.width,screen.height);var g=event.getGuiGraphics();if(box.height()<48)return;g.fill(box.x(),box.y(),box.x()+box.width(),box.y()+box.height(),0xDA181D23);
  boolean compact=box.height()<82;
  g.drawCenteredString(mc.font,compact?"Body · active/treated · Cracked Burnt Opened":"BODY RECORD · Cracked / Burnt / Opened",box.x()+box.width()/2,box.y()+3,BodyScreen.INK);
  int columns=compact?3:box.width()>=440?3:2,rows=(6+columns-1)/columns,cellW=(box.width()-12)/columns,cellH=(box.height()-14)/rows;
  for(var region:Region.values()){int i=region.ordinal();if(!InjuryClientConfig.REDUCED_MOTION.get()&&System.currentTimeMillis()-opened<i*80L)continue;int x=box.x()+6+(i%columns)*cellW,y=box.y()+14+(i/columns)*cellH;var r=body.region(region);
   g.drawString(mc.font,BodyView.label(region),x,y,r.total()>0?BodyScreen.RED:BodyScreen.GREEN,false);
   if(compact)g.drawString(mc.font,r.cracked()+"/"+r.treatedCracked()+"  "+r.burnt()+"/"+r.treatedBurnt()+"  "+r.opened()+"/"+r.treatedOpened(),x,y+9,BodyScreen.INK,false);
   else {
    g.drawString(mc.font,"Active "+r.cracked()+" / "+r.burnt()+" / "+r.opened(),x,y+9,BodyScreen.RED,false);
    g.drawString(mc.font,"Treated "+r.treatedCracked()+" / "+r.treatedBurnt()+" / "+r.treatedOpened(),x,y+18,BodyScreen.GREEN,false);
   }
  }
 }
}
