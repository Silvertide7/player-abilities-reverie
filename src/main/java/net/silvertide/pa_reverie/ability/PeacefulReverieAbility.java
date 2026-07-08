package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FluidState;
import net.silvertide.pa_reverie.effect.PeacefulReverieEffect;
import net.silvertide.pa_reverie.registry.ReverieEffects;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class PeacefulReverieAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int[] EFFECT_DURATION_TICKS_BY_LEVEL = {6000, 8400, 12000};
    private static final int REQUIRED_TOTAL_WATER_SOURCES = 30;
    private static final int REQUIRED_TOP_LAYER_SOURCES = 10;
    private static final int SCAN_FORWARD_BLOCKS = 8;
    private static final int SCAN_LEFT_BLOCKS = 3;
    private static final int SCAN_RIGHT_BLOCKS = 4;
    private static final int SCAN_DEPTH_LAYERS = 3;
    private static final int CHANNEL_PARTICLE_TICK_INTERVAL = 10;
    private static final int CHANNEL_PARTICLE_COUNT = 1;
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
        PeacefulReverieEffect.clearLockFor(player);
        Direction forward = player.getDirection();
        Direction right = forward.getClockWise();
        BlockPos feet = player.blockPosition();
        int totalSources = 0;
        int topLayerSources = 0;
        for (int forwardOffset = 0; forwardOffset < SCAN_FORWARD_BLOCKS; forwardOffset++) {
            for (int lateralOffset = -SCAN_LEFT_BLOCKS; lateralOffset <= SCAN_RIGHT_BLOCKS; lateralOffset++) {
                for (int depth = 1; depth <= SCAN_DEPTH_LAYERS; depth++) {
                    BlockPos scanned = feet.relative(forward, forwardOffset)
                            .relative(right, lateralOffset).below(depth);
                    FluidState fluid = player.level().getFluidState(scanned);
                    if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                        totalSources++;
                        if (depth == 1) {
                            topLayerSources++;
                        }
                    }
                }
            }
        }
        return totalSources >= REQUIRED_TOTAL_WATER_SOURCES && topLayerSources >= REQUIRED_TOP_LAYER_SOURCES;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.peaceful_reverie_no_water");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (player.tickCount % CHANNEL_PARTICLE_TICK_INTERVAL == 0) {
            player.serverLevel().sendParticles(ParticleTypes.CHERRY_LEAVES,
                    player.getX(), player.getY() + player.getBbHeight() + CHANNEL_PARTICLE_VERTICAL_OFFSET_ABOVE_HEAD, player.getZ(),
                    CHANNEL_PARTICLE_COUNT, CHANNEL_PARTICLE_HORIZONTAL_SPREAD, 0.0, CHANNEL_PARTICLE_HORIZONTAL_SPREAD, 0.0);
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        int clampedIndex = Math.clamp(level, 1, getMaxLevel()) - 1;
        int durationTicks = EFFECT_DURATION_TICKS_BY_LEVEL[clampedIndex];
        PeacefulReverieEffect.lockPositionFor(player);
        player.addEffect(new MobEffectInstance(ReverieEffects.PEACEFUL_REVERIE_EFFECT,
                durationTicks, clampedIndex, false, false, true));
        if (level >= getMaxLevel()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, durationTicks, 0, false, false, true));
        }
    }
}
