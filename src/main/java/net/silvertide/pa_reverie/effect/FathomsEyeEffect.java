package net.silvertide.pa_reverie.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.silvertide.pa_reverie.registry.ReverieEffects;

public class FathomsEyeEffect extends VisionEffect {

    public FathomsEyeEffect(MobEffectCategory category, int displayColor) {
        super(category, displayColor);
    }

    @Override
    protected Holder<MobEffect> getEffectHolder() {
        return ReverieEffects.FATHOMS_EYE;
    }

    public static float getIntensity(Player player, float partialTicks) {
        return VisionEffect.getIntensity(player, ReverieEffects.FATHOMS_EYE, partialTicks);
    }
}
