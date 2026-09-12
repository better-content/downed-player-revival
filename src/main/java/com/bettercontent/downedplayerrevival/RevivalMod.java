package com.bettercontent.downedplayerrevival;

import com.bettercontent.downedplayerrevival.gametest.RevivalGameTests;
import com.bettercontent.downedplayerrevival.client.InjuryClientConfig;
import com.bettercontent.downedplayerrevival.network.RevivalNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RevivalMod.MOD_ID)
public final class RevivalMod {
    public static final String MOD_ID = "downed_player_revival";
    public RevivalMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RevivalConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InjuryClientConfig.SPEC);
        InjuryItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerGameTests);
        RevivalNetwork.register();
        MinecraftForge.EVENT_BUS.register(RevivalForgeEvents.class);
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(RevivalGameTests.class);
    }
}
