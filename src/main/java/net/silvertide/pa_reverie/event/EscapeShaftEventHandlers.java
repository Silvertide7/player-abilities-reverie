package net.silvertide.pa_reverie.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.entity.EscapeShaftRiseEntity;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class EscapeShaftEventHandlers {

    private EscapeShaftEventHandlers() {}

    @SubscribeEvent
    public static void cancelDamageWhileRidingEscapeShaft(LivingIncomingDamageEvent event) {
        if (event.getEntity().getVehicle() instanceof EscapeShaftRiseEntity) {
            event.setCanceled(true);
        }
    }
}
