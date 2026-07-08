package net.silvertide.pa_reverie.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class FathomsEyeAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 300;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 20;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        return player.isEyeInFluid(FluidTags.WATER);
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.fathoms_eye_no_water");
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (player.isEyeInFluid(FluidTags.WATER)) {
            player.addEffect(new MobEffectInstance(ReverieEffects.FATHOMS_EYE,
                    byLevel(level, 3600, 7200, 18000), level - 1, false, false, true));
        }
    }
}
