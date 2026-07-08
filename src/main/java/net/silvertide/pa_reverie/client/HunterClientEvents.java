package net.silvertide.pa_reverie.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.silvertide.pa_reverie.PAReverie;

@EventBusSubscriber(modid = PAReverie.MOD_ID, value = Dist.CLIENT)
public final class HunterClientEvents {

    private HunterClientEvents() {}

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            HunterRenderState.clear();
        }
    }
}
