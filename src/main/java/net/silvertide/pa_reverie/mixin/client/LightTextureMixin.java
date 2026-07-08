package net.silvertide.pa_reverie.mixin.client;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import net.silvertide.pa_reverie.client.VisionEffectClientCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    private static final float DEEP_SIGHT_DARK_FLOOR_BRIGHTNESS = 0.9f;
    private static final float FATHOMS_EYE_DARK_FLOOR_BRIGHTNESS = 0.7f;

    @Inject(method = "getBrightness", at = @At("TAIL"), cancellable = true)
    private static void ror$boostBrightnessForVisionEffects(DimensionType dimension, int light, CallbackInfoReturnable<Float> cir) {
        float currentBrightness = cir.getReturnValueF();
        float boosted = currentBrightness;

        if (VisionEffectClientCache.isDeepSightGateOpen()) {
            boosted = Math.max(boosted, DEEP_SIGHT_DARK_FLOOR_BRIGHTNESS * VisionEffectClientCache.getDeepSightTickEndIntensity());
        }

        if (VisionEffectClientCache.isFathomsEyeGateOpen()) {
            boosted = Math.max(boosted, FATHOMS_EYE_DARK_FLOOR_BRIGHTNESS * VisionEffectClientCache.getFathomsEyeTickEndIntensity());
        }

        if (boosted != currentBrightness) {
            cir.setReturnValue(boosted);
        }
    }
}
