package com.bettercontent.downedplayerrevival;

import com.bettercontent.downedplayerrevival.gametest.RevivalGameTests;
import com.bettercontent.downedplayerrevival.network.RevivalNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RevivalMod.MOD_ID)
public final class RevivalMod {
    public static final String MOD_ID = "downed_player_revival";
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    public static final RegistryObject<SoundEvent> DOWNED_WHISPER = SOUNDS.register(
            "downed_whisper",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MOD_ID, "downed_whisper"), 32.0f)
    );

    public RevivalMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RevivalConfig.SPEC);
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerGameTests);
        RevivalNetwork.register();
        MinecraftForge.EVENT_BUS.register(RevivalForgeEvents.class);
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(RevivalGameTests.class);
    }
}
