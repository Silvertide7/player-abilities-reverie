package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.pa_reverie.effect.RestfulMeditationEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.player_abilities.api.AbilityUseType;
import net.silvertide.player_abilities.api.EffectGrant;

import java.util.List;

public final class RestfulMeditationAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int[] EFFECT_DURATION_TICKS_BY_LEVEL = {600, 900, 1200};
    private static final int CHANNEL_PARTICLE_TICK_INTERVAL = 10;
    private static final int CHANNEL_PARTICLE_COUNT = 2;
    private static final double CHANNEL_PARTICLE_HORIZONTAL_SPREAD = 1.5;
    private static final double CHANNEL_PARTICLE_VERTICAL_OFFSET_ABOVE_HEAD = 0.5;

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
    public boolean canUse(ServerPlayer player, int level) {
        RestfulMeditationEffect.clearLockFor(player);
        return true;
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (player.tickCount % CHANNEL_PARTICLE_TICK_INTERVAL == 0) {
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + player.getBbHeight() + CHANNEL_PARTICLE_VERTICAL_OFFSET_ABOVE_HEAD, player.getZ(),
                    CHANNEL_PARTICLE_COUNT, CHANNEL_PARTICLE_HORIZONTAL_SPREAD, 0.0, CHANNEL_PARTICLE_HORIZONTAL_SPREAD, 0.0);
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        RestfulMeditationEffect.lockPositionFor(player);
    }

    @Override
    public List<EffectGrant> getEffectGrants(int level) {
        int amplifier = Math.clamp(level, 1, getMaxLevel()) - 1;
        return List.of(new EffectGrant(ReverieEffects.RESTFUL_MEDITATION_EFFECT,
                byLevel(level, EFFECT_DURATION_TICKS_BY_LEVEL), amplifier, false, true));
    }
}
