package net.silvertide.pa_reverie.support;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.silvertide.player_abilities.api.AbilityAPI;

public final class AbilityPower {
    private static final double MAX_POWER_MULTIPLIER = 2.0;

    private AbilityPower() {
    }

    public static double raw(LivingEntity caster) {
        return caster instanceof ServerPlayer player ? AbilityAPI.getAbilityPower(player) : 1.0;
    }

    public static int scaled(LivingEntity caster, int base) {
        return (int) Math.round(base * cappedMultiplier(caster));
    }

    public static double scaled(LivingEntity caster, double base) {
        return base * cappedMultiplier(caster);
    }

    private static double cappedMultiplier(LivingEntity caster) {
        return Math.min(MAX_POWER_MULTIPLIER, raw(caster));
    }
}
