package net.silvertide.pa_reverie.ability;

import net.minecraft.util.Mth;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.player_abilities.api.AbilityUseType;
import net.silvertide.player_abilities.api.EffectGrant;

import java.util.List;

public final class ShepherdsAuraAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 600;
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
    public List<EffectGrant> getEffectGrants(int level) {
        return List.of(new EffectGrant(ReverieEffects.SHEPHERDS_AURA.get(),
                byLevel(level, DURATION_TICKS_BY_LEVEL), Mth.clamp(level - 1, 0, getMaxLevel() - 1), false, true));
    }
}
