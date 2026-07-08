package net.silvertide.pa_reverie.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.effect.PeacefulReverieEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class PeacefulReverieEventHandlers {

    private static final double[] VISIBILITY_BY_AMPLIFIER = { 0.33, 0.20, 0.07 };

    private PeacefulReverieEventHandlers() {}

    @SubscribeEvent
    public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (!(event.getEntity() instanceof Player reverieCaster)) {
            return;
        }
        if (!PeacefulReverieEffect.isActiveOn(reverieCaster)) {
            return;
        }
        double currentModifier = event.getVisibilityModifier();
        if (currentModifier <= 0.0) {
            return;
        }
        int amplifier = PeacefulReverieEffect.amplifierFor(reverieCaster);
        double targetVisibility = VISIBILITY_BY_AMPLIFIER[Math.clamp(amplifier, 0, VISIBILITY_BY_AMPLIFIER.length - 1)];
        event.modifyVisibility(targetVisibility / currentModifier);
    }

    @SubscribeEvent
    public static void onReverieEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) {
            return;
        }
        if (event.getEffectInstance().getEffect().value() != ReverieEffects.PEACEFUL_REVERIE_EFFECT.value()) {
            return;
        }
        PeacefulReverieEffect.onReverieEnded(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PeacefulReverieEffect.cleanupOnLogout(event.getEntity().getUUID());
    }
}
