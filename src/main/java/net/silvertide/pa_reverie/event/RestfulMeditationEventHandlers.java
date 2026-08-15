package net.silvertide.pa_reverie.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.effect.RestfulMeditationEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@Mod.EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class RestfulMeditationEventHandlers {

    private RestfulMeditationEventHandlers() {}

    @SubscribeEvent
    public static void onMeditationEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) {
            return;
        }
        if (event.getEffectInstance().getEffect() != ReverieEffects.RESTFUL_MEDITATION_EFFECT.get()) {
            return;
        }
        RestfulMeditationEffect.onMeditationEnded(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RestfulMeditationEffect.cleanupOnLogout(event.getEntity().getUUID());
    }
}
