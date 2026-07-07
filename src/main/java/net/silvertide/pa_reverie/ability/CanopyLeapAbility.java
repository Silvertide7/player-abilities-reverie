package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.silvertide.pa_reverie.support.CanopyScanner;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class CanopyLeapAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 300;
    private static final int LANDING_CLEARANCE_BLOCKS = 4;

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
        BlockPos treeHit = raycastTree(player, level);
        if (treeHit == null) {
            AbilityAPI.setUseData(player, "message.pa_reverie.canopy_leap_no_tree");
            return false;
        }
        CanopyScanner.CanopyTop canopyTop = CanopyScanner.findCanopyTop(player.serverLevel(), treeHit,
                byLevel(level, 64, 96, 128));
        if (canopyTop.result() != CanopyScanner.Result.SUCCESS) {
            AbilityAPI.setUseData(player, canopyTop.result() == CanopyScanner.Result.NOT_A_TREE
                    ? "message.pa_reverie.canopy_leap_not_living" : "message.pa_reverie.canopy_leap_too_tall");
            return false;
        }
        BlockPos top = canopyTop.top();
        for (int clearance = 1; clearance <= LANDING_CLEARANCE_BLOCKS; clearance++) {
            if (!player.serverLevel().getBlockState(top.above(clearance)).isAir()) {
                AbilityAPI.setUseData(player, "message.pa_reverie.canopy_leap_no_landing");
                return false;
            }
        }
        AbilityAPI.setUseData(player, new Vec3(top.getX() + 0.5, top.getY() + 3, top.getZ() + 0.5));
        return true;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return AbilityAPI.getUseData(player) instanceof String failureKey
                ? Component.translatable(failureKey)
                : Component.translatable("message.pa_reverie.canopy_leap_no_tree");
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (!(AbilityAPI.getUseData(player) instanceof Vec3 destination)) {
            return;
        }
        Vec3 origin = player.position();
        player.serverLevel().sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                origin.x, origin.y + 1, origin.z, 24, 0.3, 0.5, 0.3, 0.0);
        player.level().playSound(null, BlockPos.containing(origin),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7f, 1.3f);
        player.stopRiding();
        player.teleportTo(player.serverLevel(), destination.x, destination.y, destination.z,
                player.getYRot(), player.getXRot());
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
        player.serverLevel().sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                destination.x, destination.y, destination.z, 24, 0.4, 0.4, 0.4, 0.0);
        player.level().playSound(null, BlockPos.containing(destination),
                SoundEvents.AZALEA_LEAVES_FALL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private BlockPos raycastTree(ServerPlayer player, int level) {
        int reach = Mth.clamp((int) Math.round((16 + 8 * (level - 1)) * AbilityAPI.getAbilityPower(player)), 16, 64);
        Vec3 eyePosition = player.getEyePosition();
        Vec3 reachEnd = eyePosition.add(player.getViewVector(1.0f).scale(reach));
        BlockHitResult hit = player.level().clip(new ClipContext(eyePosition, reachEnd,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        var state = player.level().getBlockState(hit.getBlockPos());
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) ? hit.getBlockPos() : null;
    }
}
