package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.silvertide.pa_reverie.support.CascadeJob;
import net.silvertide.pa_reverie.support.CropTargeting;
import net.silvertide.pa_reverie.support.VerdantCascadeCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityTickJobs;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class VerdantCascadeAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 3600;
    private static final int BASE_SPELL_POWER = 100;
    private static final int SPELL_POWER_PER_LEVEL = 100;
    private static final int MIN_CHAIN = 100;
    private static final int MAX_CHAIN = 600;
    private static final double TARGET_RAYCAST_DISTANCE = 8.0;
    private static final int OUTLINE_PARTICLE_TICK_INTERVAL = 10;
    private static final int OUTLINE_PARTICLES_PER_EDGE = 1;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 40;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        return raycastCropTarget(player.level(), player) != null;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.verdant_cascade_no_crop");
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        ServerLevel serverLevel = player.serverLevel();
        VerdantCascadeCastData castData;
        if (AbilityAPI.getUseData(player) instanceof VerdantCascadeCastData existing) {
            castData = existing;
        } else {
            castData = new VerdantCascadeCastData();
            AbilityAPI.setUseData(player, castData);
            BlockPos initialTarget = raycastCropTarget(player.level(), player);
            if (initialTarget != null) {
                castData.setLockedTarget(initialTarget);
            }
        }
        if (castData.hasLockedTarget() && player.tickCount % OUTLINE_PARTICLE_TICK_INTERVAL == 0) {
            spawnTargetOutlineParticles(serverLevel, castData.getLockedTarget());
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        BlockPos target = resolveCascadeOrigin(player);
        if (target != null) {
            CascadeJob job = new CascadeJob(player, player.serverLevel(), target, chainMax(player, level), level);
            AbilityTickJobs.schedule(player, CascadeJob.PROCESS_INTERVAL_TICKS, serverPlayer -> job.processWave());
        }
    }

    private BlockPos resolveCascadeOrigin(ServerPlayer player) {
        if (AbilityAPI.getUseData(player) instanceof VerdantCascadeCastData castData
                && castData.hasLockedTarget()) {
            return castData.getLockedTarget();
        }
        return raycastCropTarget(player.level(), player);
    }

    private static void spawnTargetOutlineParticles(ServerLevel level, BlockPos pos) {
        double x0 = pos.getX();
        double y0 = pos.getY();
        double z0 = pos.getZ();
        double x1 = x0 + 1.0;
        double y1 = y0 + 1.0;
        double z1 = z0 + 1.0;
        double[][] edges = {
                {x0, y0, z0, x1, y0, z0}, {x0, y0, z1, x1, y0, z1},
                {x0, y1, z0, x1, y1, z0}, {x0, y1, z1, x1, y1, z1},
                {x0, y0, z0, x0, y1, z0}, {x0, y0, z1, x0, y1, z1},
                {x1, y0, z0, x1, y1, z0}, {x1, y0, z1, x1, y1, z1},
                {x0, y0, z0, x0, y0, z1}, {x0, y1, z0, x0, y1, z1},
                {x1, y0, z0, x1, y0, z1}, {x1, y1, z0, x1, y1, z1}
        };
        for (double[] edge : edges) {
            for (int i = 1; i <= OUTLINE_PARTICLES_PER_EDGE; i++) {
                double t = (double) i / (OUTLINE_PARTICLES_PER_EDGE + 1);
                double px = edge[0] + (edge[3] - edge[0]) * t;
                double py = edge[1] + (edge[4] - edge[1]) * t;
                double pz = edge[2] + (edge[5] - edge[2]) * t;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private int chainMax(ServerPlayer player, int level) {
        return Math.clamp(Math.round(spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level)),
                MIN_CHAIN, MAX_CHAIN);
    }

    private static BlockPos raycastCropTarget(Level level, ServerPlayer entity) {
        Vec3 eye = entity.getEyePosition();
        Vec3 lookDirection = entity.getViewVector(1.0F);
        Vec3 reach = eye.add(lookDirection.x * TARGET_RAYCAST_DISTANCE,
                lookDirection.y * TARGET_RAYCAST_DISTANCE, lookDirection.z * TARGET_RAYCAST_DISTANCE);
        ClipContext clip = new ClipContext(eye, reach, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity);
        BlockHitResult blockHit = level.clip(clip);
        if (blockHit.getType() == HitResult.Type.MISS) {
            return null;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!CropTargeting.isHarvestable(state)) {
            return null;
        }
        return pos;
    }
}
