package net.silvertide.pa_reverie.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class ShepherdsAuraEventHandlers {

    private ShepherdsAuraEventHandlers() {}

    @SubscribeEvent
    public static void onAnimalChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Animal)) {
            return;
        }
        if (event.getNewAboutToBeSetTarget() instanceof Player player
                && player.hasEffect(ReverieEffects.SHEPHERDS_AURA)) {
            event.setCanceled(true);
        }
    }
}
