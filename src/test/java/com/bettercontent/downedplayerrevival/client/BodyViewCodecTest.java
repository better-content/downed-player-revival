package com.bettercontent.downedplayerrevival.client;

import com.bettercontent.downedplayerrevival.network.BodyView;
import com.bettercontent.downedplayerrevival.state.Region;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BodyViewCodecTest {
    @Test void boundedPagesPreserveAggregatesAndAuthoritativeTuningResults() {
        var regions = Arrays.stream(Region.values()).map(r -> new BodyView.RegionView(r,
            100_000, 200_000, 300_000, .137, 800_000, 700_000, 600_000)).toList();
        var original = new BodyView(UUID.randomUUID(), "Teammate", 0, 37, true, 32, 12, 3600,
            .42, .75, 7.33, regions, List.of(new BodyView.TreatmentEntry(120003,"mod:custom_splint",com.bettercontent.downedplayerrevival.state.MaimType.CRACKED)),
            20_000, 400_000, List.of(0, 64, 1));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            BodyView.write(original, buffer);
            assertEquals(original, BodyView.read(buffer));
            assertEquals(0, buffer.readableBytes());
            assertEquals("3 min", original.traumaLifetimeLabel());
            assertEquals(2_100_000, original.region(Region.HEAD).treated());
        } finally { buffer.release(); }
    }

    @Test void oversizedHistoryPayloadIsRejectedInsteadOfAllocatingUnboundedClientData() {
        var regions = Arrays.stream(Region.values()).map(r -> new BodyView.RegionView(r, 0,0,0,0,0,0,0)).toList();
        var original = new BodyView(UUID.randomUUID(), "Review", 20,20,false,0,0,1200,0,.5,2,
            regions, Collections.nCopies(13,new BodyView.TreatmentEntry(1,"minecraft:stick",com.bettercontent.downedplayerrevival.state.MaimType.CRACKED)),0,1,List.of(0,0,0));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try { assertEquals("1 min", original.traumaLifetimeLabel()); BodyView.write(original,buffer); assertThrows(IllegalArgumentException.class,()->BodyView.read(buffer)); }
        finally { buffer.release(); }
    }
}
