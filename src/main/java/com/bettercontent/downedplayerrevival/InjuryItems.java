package com.bettercontent.downedplayerrevival;
import com.bettercontent.downedplayerrevival.state.MaimType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.*;
public final class InjuryItems {
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,RevivalMod.MOD_ID);
 public static final RegistryObject<Item> BALM=ITEMS.register("balm",()->new Item(new Item.Properties().stacksTo(16)));
 public static final RegistryObject<Item> SOOCHER=ITEMS.register("soocher",()->new Item(new Item.Properties()));
 public static TagKey<Item> treatmentTag(MaimType type){return TagKey.create(net.minecraft.core.registries.Registries.ITEM,new ResourceLocation(RevivalMod.MOD_ID,"treatments/"+type.name().toLowerCase(java.util.Locale.ROOT)));}
 public static String cureName(MaimType type){return switch(type){case CRACKED->"Stick";case BURNT->"Balm";case OPENED->"Soocher";};}
}
