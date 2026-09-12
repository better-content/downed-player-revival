package com.bettercontent.downedplayerrevival.network;
import com.bettercontent.downedplayerrevival.RevivalManager;
import com.bettercontent.downedplayerrevival.state.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.*;
import java.util.function.Supplier;
public record BodyActionPacket(UUID subject,int action,int region,int type,int page,boolean history) {
 private static final int OPEN_OVERVIEW=3;
 public static BodyActionPacket overview(UUID subject){return new BodyActionPacket(subject,OPEN_OVERVIEW,0,0,0,false);}
 public static void encode(BodyActionPacket p,FriendlyByteBuf b){b.writeUUID(p.subject);b.writeVarInt(p.action);b.writeVarInt(p.region);b.writeVarInt(p.type);b.writeVarInt(p.page);b.writeBoolean(p.history);}
 public static BodyActionPacket decode(FriendlyByteBuf b){return new BodyActionPacket(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean());}
 public static void handle(BodyActionPacket p,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->{
  var viewer=c.get().getSender();if(viewer==null)return;
  if(p.action==2){if(RevivalNetwork.isViewing(viewer,p.subject))RevivalManager.closeBody(viewer);return;}
  var subject=viewer.server.getPlayerList().getPlayer(p.subject);if(subject==null||!RevivalNetwork.canInspect(viewer,subject))return;
  if(p.action==OPEN_OVERVIEW){RevivalManager.openBody(viewer,subject);return;}
  if(p.region<0||p.region>=Region.values().length||p.type<0||p.type>=MaimType.values().length||p.page<0)return;
  if(p.action==0)RevivalNetwork.sendBody(viewer,subject,RevivalManager.snapshot(subject),Region.values()[p.region],p.history,p.page);
  else if(p.action==1)RevivalManager.startTreatment(viewer,subject,Region.values()[p.region],MaimType.values()[p.type]);
 });c.get().setPacketHandled(true);}
}
