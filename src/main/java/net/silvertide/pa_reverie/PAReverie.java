package net.silvertide.pa_reverie;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.silvertide.pa_reverie.registry.ReverieAbilities;
import net.silvertide.pa_reverie.registry.ReverieBlocks;
import net.silvertide.pa_reverie.registry.ReverieDataComponents;
import net.silvertide.pa_reverie.registry.ReverieItems;

@Mod(PAReverie.MOD_ID)
public class PAReverie {
    public static final String MOD_ID = "pa_reverie";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public PAReverie(IEventBus modEventBus) {
        ReverieAbilities.register(modEventBus);
        ReverieItems.register(modEventBus);
        ReverieBlocks.register(modEventBus);
        ReverieDataComponents.register(modEventBus);
    }
}
