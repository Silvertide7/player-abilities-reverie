package net.silvertide.pa_reverie.event;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.effect.PeacefulReverieEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@Mod.EventBusSubscriber(modid = PAReverie.MOD_ID)
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
        double targetVisibility = VISIBILITY_BY_AMPLIFIER[Mth.clamp(amplifier, 0, VISIBILITY_BY_AMPLIFIER.length - 1)];
        event.modifyVisibility(targetVisibility / currentModifier);
    }

    @SubscribeEvent
    public static void onReverieEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) {
            return;
        }
        if (event.getEffectInstance().getEffect() != ReverieEffects.PEACEFUL_REVERIE_EFFECT.get()) {
            return;
        }
        PeacefulReverieEffect.onReverieEnded(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PeacefulReverieEffect.cleanupOnLogout(event.getEntity().getUUID());
    }
}
