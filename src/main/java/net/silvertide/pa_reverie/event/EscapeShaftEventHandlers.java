package net.silvertide.pa_reverie.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.entity.EscapeShaftRiseEntity;

@Mod.EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class EscapeShaftEventHandlers {

    private EscapeShaftEventHandlers() {}

    @SubscribeEvent
    public static void cancelDamageWhileRidingEscapeShaft(LivingAttackEvent event) {
        if (event.getEntity().getVehicle() instanceof EscapeShaftRiseEntity) {
            event.setCanceled(true);
        }
    }
}
