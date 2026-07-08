package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.silvertide.pa_reverie.registry.ReverieBlocks;
import net.silvertide.pa_reverie.support.CaissonCastData;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.pa_reverie.support.SphereOffsets;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class CaissonAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 600;
    private static final int CAST_TIME_TICKS = 40;
    private static final int BASE_SPELL_POWER = 3;
    private static final int SPELL_POWER_PER_LEVEL = 3;
    private static final int MIN_RADIUS = 3;
    private static final int MAX_RADIUS = 12;
    private static final int[] LIFETIME_TICKS_BY_LEVEL = {600, 1200, 1800};
    private static final int COLLAPSE_BLOCKS_PER_TICK = 64;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return CAST_TIME_TICKS;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        return player.isInWater();
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.caisson_no_water");
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        player.setAirSupply(player.getMaxAirSupply());
        List<Vec3i> offsets = SphereOffsets.sortedOffsetsForRadius(bubbleRadius(player, level));
        CaissonCastData data;
        if (AbilityAPI.getUseData(player) instanceof CaissonCastData existing) {
            data = existing;
        } else {
            data = new CaissonCastData();
            AbilityAPI.setUseData(player, data);
        }
        if (!data.isInitialized()) {
            data.initialize(player.blockPosition());
        }
        int elapsed = data.incrementElapsedTicks();
        int targetIndex = (int) ((long) offsets.size() * elapsed / CAST_TIME_TICKS);
        convertUpTo(player.serverLevel(), data, offsets, targetIndex,
                bubbleLifetimeTicks(player, level), CAST_TIME_TICKS - elapsed);
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (AbilityAPI.getUseData(player) instanceof CaissonCastData data && data.isInitialized()) {
            List<Vec3i> offsets = SphereOffsets.sortedOffsetsForRadius(bubbleRadius(player, level));
            int ticksRemaining = Math.max(0, CAST_TIME_TICKS - data.getElapsedTicks());
            convertUpTo(player.serverLevel(), data, offsets, offsets.size(),
                    bubbleLifetimeTicks(player, level), ticksRemaining);
        }
    }

    @Override
    public void onUseComplete(ServerPlayer player, int level, boolean cancelled) {
        if (cancelled && !player.isCreative()
                && AbilityAPI.getUseData(player) instanceof CaissonCastData data
                && data.getNextOffsetIndex() > 0) {
            AbilityAPI.setCooldown(player, this, getCooldownTicks(level));
        }
    }

    private void convertUpTo(ServerLevel level, CaissonCastData data, List<Vec3i> offsets,
                             int targetIndex, int lifetimeTicks, int ticksUntilCastComplete) {
        BlockPos center = data.getCenter();
        Block dryAirBlock = ReverieBlocks.DRY_AIR.get();
        BlockState dryAir = dryAirBlock.defaultBlockState();
        int outermostIndex = offsets.size() - 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int index = data.getNextOffsetIndex();
        int end = Math.min(offsets.size(), targetIndex);
        for (; index < end; index++) {
            Vec3i offset = offsets.get(index);
            cursor.set(center.getX() + offset.getX(), center.getY() + offset.getY(), center.getZ() + offset.getZ());
            if (level.getBlockState(cursor).is(Blocks.WATER)) {
                BlockPos pos = cursor.immutable();
                level.setBlock(pos, dryAir, Block.UPDATE_CLIENTS);
                int collapseStagger = (outermostIndex - index) / COLLAPSE_BLOCKS_PER_TICK;
                level.scheduleTick(pos, dryAirBlock, ticksUntilCastComplete + lifetimeTicks + collapseStagger);
            }
        }
        data.advanceTo(index);
    }

    private int bubbleRadius(ServerPlayer player, int level) {
        return Math.clamp(Math.round(spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level)),
                MIN_RADIUS, MAX_RADIUS);
    }

    private int bubbleLifetimeTicks(ServerPlayer player, int level) {
        return ReverieMagicAttributes.scaledByHarvestPower(player,
                LIFETIME_TICKS_BY_LEVEL[Math.clamp(level, 1, getMaxLevel()) - 1]);
    }
}
