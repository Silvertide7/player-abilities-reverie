package net.silvertide.pa_reverie.client;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.effect.DeepSightEffect;
import net.silvertide.pa_reverie.effect.FathomsEyeEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@EventBusSubscriber(modid = PAReverie.MOD_ID, value = Dist.CLIENT)
public final class VisionEffectClientCache {

    private static boolean deepSightGateOpen = false;
    private static float deepSightTickEndIntensity = 0.0f;
    private static boolean fathomsEyeGateOpen = false;
    private static float fathomsEyeTickEndIntensity = 0.0f;

    private VisionEffectClientCache() {}

    public static boolean isDeepSightGateOpen() {
        return deepSightGateOpen;
    }

    public static float getDeepSightTickEndIntensity() {
        return deepSightTickEndIntensity;
    }

    public static boolean isFathomsEyeGateOpen() {
        return fathomsEyeGateOpen;
    }

    public static float getFathomsEyeTickEndIntensity() {
        return fathomsEyeTickEndIntensity;
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            clearAll();
            return;
        }
        recomputeDeepSightGate(player);
        recomputeFathomsEyeGate(player);
    }

    private static void recomputeDeepSightGate(Player player) {
        MobEffectInstance effect = player.getEffect(ReverieEffects.DEEP_SIGHT);
        if (effect == null || !DeepSightEffect.isSufficientlyUnderground(player.level(), player)) {
            deepSightGateOpen = false;
            deepSightTickEndIntensity = 0.0f;
            return;
        }
        deepSightGateOpen = true;
        deepSightTickEndIntensity = DeepSightEffect.getIntensity(player, 1.0f);
    }

    private static void recomputeFathomsEyeGate(Player player) {
        MobEffectInstance effect = player.getEffect(ReverieEffects.FATHOMS_EYE);
        if (effect == null || !player.isEyeInFluid(FluidTags.WATER)) {
            fathomsEyeGateOpen = false;
            fathomsEyeTickEndIntensity = 0.0f;
            return;
        }
        fathomsEyeGateOpen = true;
        fathomsEyeTickEndIntensity = FathomsEyeEffect.getIntensity(player, 1.0f);
    }

    private static void clearAll() {
        deepSightGateOpen = false;
        deepSightTickEndIntensity = 0.0f;
        fathomsEyeGateOpen = false;
        fathomsEyeTickEndIntensity = 0.0f;
    }
}
