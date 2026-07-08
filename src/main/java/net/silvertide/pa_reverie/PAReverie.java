package net.silvertide.pa_reverie;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.silvertide.pa_reverie.config.ServerConfigs;
import net.silvertide.pa_reverie.food.ConjuredFoods;
import net.silvertide.pa_reverie.registry.ReverieAbilities;
import net.silvertide.pa_reverie.registry.ReverieBlocks;
import net.silvertide.pa_reverie.registry.ReverieDataComponents;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.pa_reverie.registry.ReverieEntities;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.pa_reverie.network.ReverieSpellNetwork;
import net.silvertide.pa_reverie.support.ExcavateManager;
import net.silvertide.pa_reverie.support.VerdantCascadeManager;
import net.silvertide.pa_reverie.transmute.TransmuteRecipes;

@Mod(PAReverie.MOD_ID)
public class PAReverie {
    public static final String MOD_ID = "pa_reverie";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public PAReverie(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC);
        ReverieAbilities.register(modEventBus);
        ConjuredFoods.register(modEventBus);
        ReverieBlocks.register(modEventBus);
        ReverieDataComponents.register(modEventBus);
        ReverieEffects.register(modEventBus);
        ReverieEntities.register(modEventBus);
        ReverieMagicAttributes.register(modEventBus);
        TransmuteRecipes.register(modEventBus);
        ReverieSpellNetwork.register(modEventBus);
        VerdantCascadeManager.register();
        ExcavateManager.register();
    }
}
