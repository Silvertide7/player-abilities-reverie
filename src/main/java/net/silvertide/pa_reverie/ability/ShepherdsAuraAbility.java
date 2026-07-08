package net.silvertide.pa_reverie.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class ShepherdsAuraAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int[] DURATION_TICKS_BY_LEVEL = {1200, 1800, 2400};

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
    public void onUseReleased(ServerPlayer player, int level) {
        int amplifier = Math.clamp(level - 1, 0, getMaxLevel() - 1);
        player.addEffect(new MobEffectInstance(ReverieEffects.SHEPHERDS_AURA,
                DURATION_TICKS_BY_LEVEL[Math.clamp(level, 1, getMaxLevel()) - 1], amplifier, false, false, true));
    }
}
