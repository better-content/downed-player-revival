package com.bettercontent.downedplayerrevival.network;
import com.bettercontent.downedplayerrevival.client.ClientRevivalState;
import com.bettercontent.downedplayerrevival.DamageLedger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record StateSyncPacket(BodyView view, int mode, int region, boolean history, DamageLedger.Summary damage) {
 public StateSyncPacket(BodyView view,int mode,int region,boolean history){this(view,mode,region,history,DamageLedger.Summary.empty());}
 public static void encode(StateSyncPacket p,FriendlyByteBuf b){BodyView.write(p.view,b);b.writeVarInt(p.mode);b.writeVarInt(p.region);b.writeBoolean(p.history);b.writeVarLong(p.damage.hits());b.writeDouble(p.damage.incoming());b.writeDouble(p.damage.mitigation());b.writeDouble(p.damage.absorption());b.writeDouble(p.damage.applied());b.writeDouble(p.damage.healthLost());b.writeDouble(p.damage.unknown());}
 public static StateSyncPacket decode(FriendlyByteBuf b){BodyView view=BodyView.read(b);int mode=b.readVarInt(),region=b.readVarInt();boolean history=b.readBoolean();return new StateSyncPacket(view,mode,region,history,new DamageLedger.Summary(b.readVarLong(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble()));}
 public static void handle(StateSyncPacket p,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ClientRevivalState.accept(p)));c.get().setPacketHandled(true);}
}
