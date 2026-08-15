package net.silvertide.pa_reverie;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silvertide.pa_reverie.config.ServerConfigs;
import net.silvertide.pa_reverie.food.ConjuredFoods;
import net.silvertide.pa_reverie.network.ReverieNetworking;
import net.silvertide.pa_reverie.registry.ReverieAbilities;
import net.silvertide.pa_reverie.registry.ReverieBlocks;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.pa_reverie.registry.ReverieEntities;
import net.silvertide.pa_reverie.transmute.TransmuteRecipes;

@Mod(PAReverie.MOD_ID)
public class PAReverie {
    public static final String MOD_ID = "pa_reverie";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public PAReverie() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC);
        ReverieNetworking.register();
        ReverieAbilities.register(modEventBus);
        ConjuredFoods.register(modEventBus);
        ReverieBlocks.register(modEventBus);
        ReverieEffects.register(modEventBus);
        ReverieEntities.register(modEventBus);
        TransmuteRecipes.register(modEventBus);
    }
}
