package net.silvertide.pa_reverie.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@Mod.EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class ShepherdsAuraEventHandlers {

    private ShepherdsAuraEventHandlers() {}

    @SubscribeEvent
    public static void onAnimalChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Animal)) {
            return;
        }
        if (event.getNewTarget() instanceof Player player
                && player.hasEffect(ReverieEffects.SHEPHERDS_AURA.get())) {
            event.setCanceled(true);
        }
    }
}
