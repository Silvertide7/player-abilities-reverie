package net.silvertide.pa_reverie.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.level.LevelEvent;
import net.silvertide.pa_reverie.PAReverie;

@Mod.EventBusSubscriber(modid = PAReverie.MOD_ID, value = Dist.CLIENT)
public final class HunterClientEvents {

    private HunterClientEvents() {}

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            HunterRenderState.clear();
        }
    }
}
