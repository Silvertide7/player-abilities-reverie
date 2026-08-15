package net.silvertide.pa_reverie.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.silvertide.pa_reverie.registry.ReverieEffects;

public class FathomsEyeEffect extends VisionEffect {

    public FathomsEyeEffect(MobEffectCategory category, int displayColor) {
        super(category, displayColor);
    }

    public static float getIntensity(Player player, float partialTicks) {
        return VisionEffect.getIntensity(player, ReverieEffects.FATHOMS_EYE.get(), partialTicks);
    }
}
