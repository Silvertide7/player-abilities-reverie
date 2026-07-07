package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class PeacefulReverieAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final double MOVEMENT_BREAK_THRESHOLD_SQ = 0.25;
    private static final int REQUIRED_TOTAL_WATER_SOURCES = 30;
    private static final int REQUIRED_TOP_LAYER_SOURCES = 10;
    private static final int SCAN_FORWARD_BLOCKS = 8;
    private static final int SCAN_LEFT_BLOCKS = 3;
    private static final int SCAN_RIGHT_BLOCKS = 4;
    private static final int SCAN_DEPTH_LAYERS = 3;

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
        return byLevel(level, 6000, 8400, 12000);
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        Direction forward = player.getDirection();
        Direction right = forward.getClockWise();
        BlockPos feet = player.blockPosition();
        int totalSources = 0;
        int topLayerSources = 0;
        for (int forwardOffset = 1; forwardOffset <= SCAN_FORWARD_BLOCKS; forwardOffset++) {
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
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (elapsedTicks % 10 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.CHERRY_LEAVES,
                    player.getX(), player.getY() + player.getBbHeight() + 0.5, player.getZ(), 1, 1.5, 0.2, 1.5, 0.0);
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (level >= getMaxLevel()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, getEffectDurationTicks(level), 0));
        }
    }

    @Override
    public void onEffectStart(ServerPlayer player, int level) {
        AbilityAPI.setEffectData(player, this, player.position());
    }

    @Override
    public void onEffectTick(ServerPlayer player, int level, int remainingTicks) {
        if (remainingTicks % 10 == 0 && AbilityAPI.getEffectData(player, this) instanceof Vec3 lockedPosition
                && player.position().distanceToSqr(lockedPosition) > MOVEMENT_BREAK_THRESHOLD_SQ) {
            AbilityAPI.removeEffect(player, this);
        }
    }

    @Override
    public void onEffectEnd(ServerPlayer player, int level, boolean expired) {
        player.removeEffect(MobEffects.INVISIBILITY);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    public static float visibilityMultiplierForLevel(int level) {
        return switch (level) {
            case 1 -> 0.33f;
            case 2 -> 0.20f;
            default -> 0.07f;
        };
    }
}
