package com.bettercontent.downedplayerrevival.client;
import com.bettercontent.downedplayerrevival.RevivalMod;
import com.bettercontent.downedplayerrevival.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=RevivalMod.MOD_ID,value=Dist.CLIENT)
public final class ClientRevivalInput {
 /** Request a fresh authoritative view, including current treatment supplies. */
 public static void openOwnBody(){var player=Minecraft.getInstance().player;if(player!=null)RevivalNetwork.CHANNEL.sendToServer(BodyActionPacket.overview(player.getUUID()));}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){ClientRevivalState.clear();}
 @SubscribeEvent public static void inventory(ScreenEvent.Init.Post event){if(event.getScreen() instanceof InventoryScreen screen){int x=Math.max(3,(screen.width-176)/2-49),y=(screen.height-166)/2;event.addListener(Button.builder(Component.literal("Body"),button->openOwnBody()).bounds(x,y,46,20).build());}}
 @SubscribeEvent public static void inventoryIcon(ScreenEvent.Render.Post event){if(event.getScreen() instanceof InventoryScreen screen){int x=Math.max(3,(screen.width-176)/2-49)+3,y=(screen.height-166)/2+3;var g=event.getGuiGraphics();int c=0xFFA8CEB2;g.fill(x+3,y,x+6,y+3,c);g.fill(x+2,y+4,x+7,y+9,c);g.fill(x,y+4,x+1,y+10,c);g.fill(x+8,y+4,x+9,y+10,c);g.fill(x+2,y+10,x+4,y+14,c);g.fill(x+5,y+10,x+7,y+14,c);}}
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered event){var mc=Minecraft.getInstance();if(event.isUseItem()&&mc.screen==null&&mc.player!=null&&mc.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof Player other&&mc.player.distanceToSqr(other)<=9){event.setCanceled(true);event.setSwingHand(false);RevivalNetwork.CHANNEL.sendToServer(BodyActionPacket.overview(other.getUUID()));}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase==TickEvent.Phase.END){ClientRevivalState.tick(Minecraft.getInstance());RevivalHud.tick();}}
}
