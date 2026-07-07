package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class RestfulMeditationAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final double MOVEMENT_BREAK_THRESHOLD_SQ = 0.25;
    private static final float HEAL_PER_PULSE = 1.0f;
    private static final int FOOD_PER_PULSE = 1;
    private static final float SATURATION_PER_PULSE = 1.0f;
    private static final int FULL_FOOD_LEVEL = 20;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return byLevel(level, 200, 160, 100);
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean requiresStationary() {
        return true;
    }

    @Override
    public int getEffectDurationTicks(int level) {
        return byLevel(level, 600, 900, 1200);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (elapsedTicks % 10 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + player.getBbHeight() + 0.5, player.getZ(), 2, 1.5, 0.2, 1.5, 0.0);
        }
    }

    @Override
    public void onEffectStart(ServerPlayer player, int level) {
        AbilityAPI.setEffectData(player, this, player.position());
    }

    @Override
    public void onEffectTick(ServerPlayer player, int level, int remainingTicks) {
        if (AbilityAPI.getEffectData(player, this) instanceof Vec3 lockedPosition
                && player.position().distanceToSqr(lockedPosition) > MOVEMENT_BREAK_THRESHOLD_SQ) {
            AbilityAPI.removeEffect(player, this);
            return;
        }
        if (remainingTicks % byLevel(level, 40, 30, 20) == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(HEAL_PER_PULSE);
        }
        if (remainingTicks % byLevel(level, 60, 45, 30) == 0 && player.getFoodData().getFoodLevel() < FULL_FOOD_LEVEL) {
            player.getFoodData().eat(FOOD_PER_PULSE, SATURATION_PER_PULSE);
        }
    }

    @Override
    public void onEffectEnd(ServerPlayer player, int level, boolean expired) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1.2f);
    }
}
