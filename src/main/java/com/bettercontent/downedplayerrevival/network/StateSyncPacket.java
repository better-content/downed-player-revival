package com.bettercontent.downedplayerrevival.network;
import com.bettercontent.downedplayerrevival.client.ClientRevivalState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record StateSyncPacket(BodyView view, int mode, int region, boolean history) {
 public static void encode(StateSyncPacket p,FriendlyByteBuf b){BodyView.write(p.view,b);b.writeVarInt(p.mode);b.writeVarInt(p.region);b.writeBoolean(p.history);}
 public static StateSyncPacket decode(FriendlyByteBuf b){return new StateSyncPacket(BodyView.read(b),b.readVarInt(),b.readVarInt(),b.readBoolean());}
 public static void handle(StateSyncPacket p,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ClientRevivalState.accept(p)));c.get().setPacketHandled(true);}
}
