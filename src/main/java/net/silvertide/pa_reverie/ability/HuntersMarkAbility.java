package net.silvertide.pa_reverie.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class HuntersMarkAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 300;
    private static final int[] DURATION_TICKS_BY_LEVEL = {600, 900, 1200};

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
        int durationTicks = ReverieMagicAttributes.scaledByHarvestPower(player,
                DURATION_TICKS_BY_LEVEL[Math.clamp(level, 1, getMaxLevel()) - 1]);
        player.addEffect(new MobEffectInstance(ReverieEffects.HUNTER_EFFECT, durationTicks, amplifier, false, false, true));
    }
}
