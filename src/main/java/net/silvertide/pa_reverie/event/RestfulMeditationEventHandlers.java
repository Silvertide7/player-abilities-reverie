package net.silvertide.pa_reverie.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.effect.RestfulMeditationEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class RestfulMeditationEventHandlers {

    private RestfulMeditationEventHandlers() {}

    @SubscribeEvent
    public static void onMeditationEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) {
            return;
        }
        if (event.getEffectInstance().getEffect().value() != ReverieEffects.RESTFUL_MEDITATION_EFFECT.value()) {
            return;
        }
        RestfulMeditationEffect.onMeditationEnded(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RestfulMeditationEffect.cleanupOnLogout(event.getEntity().getUUID());
    }
}
