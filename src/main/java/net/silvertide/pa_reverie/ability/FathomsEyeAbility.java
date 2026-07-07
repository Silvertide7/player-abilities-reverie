package net.silvertide.pa_reverie.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.silvertide.player_abilities.api.AbilityUseType;
import net.silvertide.player_abilities.api.EffectGrant;

import java.util.List;

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
    public List<EffectGrant> getEffectGrants(int level) {
        return List.of(new EffectGrant(MobEffects.NIGHT_VISION, byLevel(level, 3600, 7200, 18000), 0),
                new EffectGrant(MobEffects.WATER_BREATHING, byLevel(level, 3600, 7200, 18000), 0));
    }
}
