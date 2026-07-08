package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

public class ExcavateJob {

    public static final int PROCESS_INTERVAL_TICKS = 4;
    private static final int TARGET_DURATION_TICKS = 40;
    private static final int TARGET_WAVE_COUNT = TARGET_DURATION_TICKS / PROCESS_INTERVAL_TICKS;
    private static final int MAX_BLOCKS_PER_WAVE = 16;
    private static final int LEVEL_EVENT_BLOCK_BREAK_PARTICLES_AND_SOUND = 2001;

    private final ServerPlayer player;
    private final ServerLevel level;
    private final List<BlockPos> positions;
    private final boolean teleportDropsToPlayer;
    private int nextIndex = 0;

    public ExcavateJob(ServerPlayer player, ServerLevel level, List<BlockPos> positions, boolean teleportDropsToPlayer) {
        this.player = player;
        this.level = level;
        this.positions = positions;
        this.teleportDropsToPlayer = teleportDropsToPlayer;
    }

    public boolean processWave() {
        if (!isPlayerStillValid()) {
            return false;
        }
        int blocksThisWave = Math.min(MAX_BLOCKS_PER_WAVE,
                Math.max(1, (positions.size() + TARGET_WAVE_COUNT - 1) / TARGET_WAVE_COUNT));
        for (int i = 0; i < blocksThisWave && !isDone(); i++) {
            processOne();
        }
        return !isDone();
    }

    private boolean isDone() {
        return nextIndex >= positions.size();
    }

    private boolean isPlayerStillValid() {
        return player.isAlive() && !player.hasDisconnected() && player.serverLevel() == level;
    }

    private void processOne() {
        if (isDone()) {
            return;
        }
        BlockPos pos = positions.get(nextIndex++);
        BlockState state = level.getBlockState(pos);
        if (!isStillExcavatable(state, pos)) {
            return;
        }
        breakAsPlayer(pos, state);
    }

    private boolean isStillExcavatable(BlockState state, BlockPos pos) {
        return !state.isAir()
                && state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                && state.getDestroySpeed(level, pos) >= 0;
    }

    private void breakAsPlayer(BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);

        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, pos, state, player);
        NeoForge.EVENT_BUS.post(breakEvent);
        if (breakEvent.isCanceled()) {
            return;
        }

        level.removeBlock(pos, false);
        level.levelEvent(LEVEL_EVENT_BLOCK_BREAK_PARTICLES_AND_SOUND, pos, Block.getId(state));
        deliverDrops(pos, drops);
    }

    private void deliverDrops(BlockPos pos, List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            if (teleportDropsToPlayer) {
                if (!player.getInventory().add(drop)) {
                    Block.popResource(level, player.blockPosition(), drop);
                }
            } else {
                Block.popResource(level, pos, drop);
            }
        }
    }
}
