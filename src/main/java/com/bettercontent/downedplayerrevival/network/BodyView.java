package com.bettercontent.downedplayerrevival.network;

import com.bettercontent.downedplayerrevival.state.*;
import net.minecraft.network.FriendlyByteBuf;
import java.util.*;

/** Bounded wire view: aggregates never truncate injuries; history is requested in pages. */
public record BodyView(UUID playerId, String name, float health, float maxHealth, boolean atDoor,
 int healingLockTicks, int trauma, int traumaLifetimeTicks, double probability, double multiplier, double treatmentSeconds,
 List<RegionView> regions, List<TreatmentEntry> history, int historyPage, int historyPages, List<Integer> supplies) {
 public record TreatmentEntry(int index,String itemId,MaimType type) {}
 public record RegionView(Region region, int cracked, int burnt, int opened, double reduction, int treatedCracked, int treatedBurnt, int treatedOpened) {
  public int count(MaimType type) { return switch(type) { case CRACKED -> cracked; case BURNT -> burnt; case OPENED -> opened; }; }
  public int total() { return cracked+burnt+opened; }
  public int treated() { return treatedCracked+treatedBurnt+treatedOpened; }
 }
 public BodyView { regions=List.copyOf(regions); history=List.copyOf(history); supplies=List.copyOf(supplies); }
 public RegionView region(Region region) { return regions.get(region.ordinal()); }
 public static void write(BodyView v,FriendlyByteBuf b) {
  b.writeUUID(v.playerId); b.writeUtf(v.name,128); b.writeFloat(v.health); b.writeFloat(v.maxHealth); b.writeBoolean(v.atDoor);
  b.writeVarInt(v.healingLockTicks); b.writeVarInt(v.trauma); b.writeVarInt(v.traumaLifetimeTicks); b.writeDouble(v.probability); b.writeDouble(v.multiplier); b.writeDouble(v.treatmentSeconds);
  for(var r:v.regions) { b.writeVarInt(r.cracked);b.writeVarInt(r.burnt);b.writeVarInt(r.opened);b.writeDouble(r.reduction);b.writeVarInt(r.treatedCracked);b.writeVarInt(r.treatedBurnt);b.writeVarInt(r.treatedOpened); }
  b.writeVarInt(v.history.size()); for(var entry:v.history){b.writeVarInt(entry.index);b.writeUtf(entry.itemId,256);b.writeEnum(entry.type);}
  b.writeVarInt(v.historyPage);b.writeVarInt(v.historyPages);for(int n:v.supplies)b.writeVarInt(n);
 }
 public static BodyView read(FriendlyByteBuf b) {
  UUID id=b.readUUID();String name=b.readUtf(128);float health=b.readFloat(),max=b.readFloat();boolean door=b.readBoolean();
  int lock=b.readVarInt(),trauma=b.readVarInt(),lifetime=b.readVarInt();double odds=b.readDouble(),m=b.readDouble(),seconds=b.readDouble();
  var regions=new ArrayList<RegionView>();for(var r:Region.values()) regions.add(new RegionView(r,b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readDouble(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
  int size=b.readVarInt();if(size<0||size>12)throw new IllegalArgumentException("Invalid history page");
  var history=new ArrayList<TreatmentEntry>();for(int i=0;i<size;i++)history.add(new TreatmentEntry(b.readVarInt(),b.readUtf(256),b.readEnum(MaimType.class)));
  int page=b.readVarInt(),pages=b.readVarInt();return new BodyView(id,name,health,max,door,lock,trauma,lifetime,odds,m,seconds,regions,history,page,pages,List.of(b.readVarInt(),b.readVarInt(),b.readVarInt()));
 }
 public String traumaLifetimeLabel() { return traumaLifetimeTicks % 1200 == 0 ? traumaLifetimeTicks / 1200 + " min" : String.format(Locale.ROOT, "%.1fs", traumaLifetimeTicks / 20.0); }
 public static String label(Enum<?> value) { String s=value.name().toLowerCase(Locale.ROOT).replace('_',' ');return Character.toUpperCase(s.charAt(0))+s.substring(1); }
}
