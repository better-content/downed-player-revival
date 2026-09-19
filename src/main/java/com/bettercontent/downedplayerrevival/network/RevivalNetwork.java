package com.bettercontent.downedplayerrevival.network;
import com.bettercontent.downedplayerrevival.*;
import com.bettercontent.downedplayerrevival.state.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;
public final class RevivalNetwork {
 private static final String PROTOCOL="4";
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(RevivalMod.MOD_ID,"main"),()->PROTOCOL,PROTOCOL::equals,PROTOCOL::equals);
 private record Viewing(UUID subject,Region region,boolean history,int page){}
 private static final Map<UUID,Viewing> VIEWERS=new HashMap<>();
 public static void clear(){VIEWERS.clear();}
 public static void register(){
 CHANNEL.registerMessage(0,StateSyncPacket.class,StateSyncPacket::encode,StateSyncPacket::decode,StateSyncPacket::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
 CHANNEL.registerMessage(1,BodyActionPacket.class,BodyActionPacket::encode,BodyActionPacket::decode,BodyActionPacket::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
 CHANNEL.registerMessage(2,UiControlPacket.class,UiControlPacket::encode,UiControlPacket::decode,UiControlPacket::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
 }
 public static boolean canInspect(ServerPlayer viewer,ServerPlayer subject){return RevivalManager.validTreatmentTarget(viewer,subject);}
 public static boolean isViewing(ServerPlayer viewer,ServerPlayer subject){return subject!=null&&isViewing(viewer,subject.getUUID());}
 public static boolean isViewing(ServerPlayer viewer,UUID subject){var v=VIEWERS.get(viewer.getUUID());return v!=null&&v.subject.equals(subject);}
 public static BodyView view(ServerPlayer viewer,ServerPlayer subject,BodySnapshot snapshot,Region selected,int requestedPage){
  var rows=new ArrayList<BodyView.RegionView>();for(var r:Region.values())rows.add(new BodyView.RegionView(r,count(snapshot,r,MaimType.CRACKED),count(snapshot,r,MaimType.BURNT),count(snapshot,r,MaimType.OPENED),snapshot.regionalReduction(r),treated(snapshot,r,MaimType.CRACKED),treated(snapshot,r,MaimType.BURNT),treated(snapshot,r,MaimType.OPENED)));
  var all=snapshot.treatmentHistory().stream().filter(t->t.region()==selected).toList();int pages=Math.max(1,(all.size()+5)/6),page=Math.max(0,Math.min(requestedPage,pages-1));
  var history=new ArrayList<BodyView.TreatmentEntry>();for(int i=page*6;i<Math.min(all.size(),(page+1)*6);i++){var t=all.get(i);history.add(new BodyView.TreatmentEntry(i+1,t.itemId(),t.type()));}
  return new BodyView(subject.getUUID(),subject.getGameProfile().getName(),snapshot.health(),snapshot.maxHealth(),snapshot.atDoor(),(int)snapshot.healingLockTicks(),snapshot.traumaCount(),snapshot.tuning().traumaLifetimeTicks(),snapshot.deathProbability(),snapshot.functionalMultiplier(),RevivalManager.snapshot(viewer).treatmentSeconds(),rows,history,page,pages,RevivalManager.treatmentPriority(viewer),RevivalManager.isTreating(viewer,subject));
 }
 private static int treated(BodySnapshot s,Region r,MaimType t){return (int)s.treatmentHistory().stream().filter(m->m.region()==r&&m.type()==t).count();}
 private static int count(BodySnapshot s,Region r,MaimType t){return (int)s.activeMaims().stream().filter(m->m.region()==r&&m.type()==t).count();}
 public static void sendBody(ServerPlayer viewer,ServerPlayer subject,BodySnapshot s){if(!canInspect(viewer,subject))return;sendBody(viewer,subject,s,Region.HEAD,false,0);send(viewer,new UiControlPacket("body-view","regions",0,0));}
 public static void sendBody(ServerPlayer viewer,ServerPlayer subject,BodySnapshot s,Region r,boolean history,int page){if(!canInspect(viewer,subject))return;VIEWERS.put(viewer.getUUID(),new Viewing(subject.getUUID(),r,history,page));send(viewer,new StateSyncPacket(view(viewer,subject,s,r,page),1,r.ordinal(),history));}
 public static void closeBody(ServerPlayer viewer){VIEWERS.remove(viewer.getUUID());}
 public static void refreshViewing(ServerPlayer viewer,ServerPlayer subject){var v=VIEWERS.get(viewer.getUUID());if(v!=null&&v.subject.equals(subject.getUUID()))sendBody(viewer,subject,RevivalManager.snapshot(subject),v.region,v.history,v.page);}
 public static void sync(ServerPlayer player,BodySnapshot s){send(player,new StateSyncPacket(view(player,player,s,Region.HEAD,0),0,0,false));for(var entry:new ArrayList<>(VIEWERS.entrySet())){var viewer=player.server.getPlayerList().getPlayer(entry.getKey());var v=entry.getValue();if(viewer==null){VIEWERS.remove(entry.getKey());continue;}if(v.subject.equals(player.getUUID())){if(canInspect(viewer,player))send(viewer,new StateSyncPacket(view(viewer,player,s,v.region,v.page),3,v.region.ordinal(),v.history));else{closeBody(viewer);send(viewer,new UiControlPacket("close","Too far away to treat",0,0));}}}}
 public static void sendRecap(ServerPlayer player,BodySnapshot s){send(player,new StateSyncPacket(view(player,player,s,Region.HEAD,0),2,0,false));}
 public static void treatmentStatus(ServerPlayer viewer,ServerPlayer subject,float progress,String message){send(viewer,new UiControlPacket("treatment",message,progress,0));}
 public static void send(ServerPlayer player,Object packet){if(player.connection==null)return;CHANNEL.send(PacketDistributor.PLAYER.with(()->player),packet);}
}
