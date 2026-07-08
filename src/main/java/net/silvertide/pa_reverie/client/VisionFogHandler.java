package net.silvertide.pa_reverie.client;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.effect.DeepSightEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;

@EventBusSubscriber(modid = PAReverie.MOD_ID, value = Dist.CLIENT)
public final class VisionFogHandler {

    private static final float[] DEEP_SIGHT_FOG_FAR_BY_AMPLIFIER = { 16.0f, 28.0f, 56.0f };
    private static final float[] FATHOMS_EYE_FOG_FAR_BY_AMPLIFIER = { 75.0f, 140.0f, 200.0f };
    private static final float FOG_NEAR_FRACTION_OF_FAR = 0.5f;

    private static final float DEEP_SIGHT_TINT_RED = 0.04f;
    private static final float DEEP_SIGHT_TINT_GREEN = 0.18f;
    private static final float DEEP_SIGHT_TINT_BLUE = 0.10f;
    private static final float DEEP_SIGHT_LEVEL_3_TINT_DAMPEN = 0.3f;
    private static final int DEEP_SIGHT_LEVEL_3_AMPLIFIER = 2;

    private VisionFogHandler() {}

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return;
        }
        if (tryApplyFathomsEyeFog(event, localPlayer)) {
            return;
        }
        tryApplyDeepSightFog(event, localPlayer);
    }

    private static boolean tryApplyFathomsEyeFog(ViewportEvent.RenderFog event, Player localPlayer) {
        if (event.getCamera().getFluidInCamera() != FogType.WATER) {
            return false;
        }
        MobEffectInstance effect = localPlayer.getEffect(ReverieEffects.FATHOMS_EYE);
        if (effect == null) {
            return false;
        }
        int amplifier = Mth.clamp(effect.getAmplifier(), 0, FATHOMS_EYE_FOG_FAR_BY_AMPLIFIER.length - 1);
        float targetFar = FATHOMS_EYE_FOG_FAR_BY_AMPLIFIER[amplifier];
        applyFogOverride(event, targetFar);
        return true;
    }

    private static boolean tryApplyDeepSightFog(ViewportEvent.RenderFog event, Player localPlayer) {
        MobEffectInstance effect = localPlayer.getEffect(ReverieEffects.DEEP_SIGHT);
        if (effect == null) {
            return false;
        }
        if (!DeepSightEffect.isSufficientlyUnderground(localPlayer.level(), localPlayer)) {
            return false;
        }
        int amplifier = Mth.clamp(effect.getAmplifier(), 0, DEEP_SIGHT_FOG_FAR_BY_AMPLIFIER.length - 1);
        applyFogOverride(event, DEEP_SIGHT_FOG_FAR_BY_AMPLIFIER[amplifier]);
        return true;
    }

    private static void applyFogOverride(ViewportEvent.RenderFog event, float targetFar) {
        event.setNearPlaneDistance(targetFar * FOG_NEAR_FRACTION_OF_FAR);
        event.setFarPlaneDistance(targetFar);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return;
        }
        MobEffectInstance effect = localPlayer.getEffect(ReverieEffects.DEEP_SIGHT);
        if (effect == null) {
            return;
        }
        if (!DeepSightEffect.isSufficientlyUnderground(localPlayer.level(), localPlayer)) {
            return;
        }
        int amplifier = Mth.clamp(effect.getAmplifier(), 0, DEEP_SIGHT_FOG_FAR_BY_AMPLIFIER.length - 1);
        float tintScale = amplifier >= DEEP_SIGHT_LEVEL_3_AMPLIFIER ? DEEP_SIGHT_LEVEL_3_TINT_DAMPEN : 1.0f;
        event.setRed(DEEP_SIGHT_TINT_RED * tintScale);
        event.setGreen(DEEP_SIGHT_TINT_GREEN * tintScale);
        event.setBlue(DEEP_SIGHT_TINT_BLUE * tintScale);
    }
}
