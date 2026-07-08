package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CascadeJob {

    public static final int PROCESS_INTERVAL_TICKS = 4;
    private static final int TARGET_DURATION_TICKS = 200;
    private static final int TARGET_WAVE_COUNT = TARGET_DURATION_TICKS / PROCESS_INTERVAL_TICKS;
    private static final int TRAIL_PARTICLE_DENSITY = 2;
    private static final int HARVEST_BURST_PARTICLE_COUNT = 8;
    private static final int PARTICLE_FREQUENCY_DIVISOR = 3;
    private static final int LEVEL_EVENT_BLOCK_BREAK_PARTICLES_AND_SOUND = 2001;

    private static final int[][] HORIZONTAL_DIAGONAL_OFFSETS = {
            {+1, 0, +1}, {+1, 0, -1}, {-1, 0, +1}, {-1, 0, -1}
    };

    private static final int[][] VERTICAL_EDGE_DIAGONAL_OFFSETS = {
            {+1, +1, 0}, {+1, -1, 0}, {-1, +1, 0}, {-1, -1, 0},
            {0, +1, +1}, {0, +1, -1}, {0, -1, +1}, {0, -1, -1}
    };

    private static final int[][] NO_ADDITIONAL_OFFSETS = new int[0][];

    private final ServerPlayer player;
    private final ServerLevel level;
    private final int chainMax;
    private final int[][] additionalNeighborOffsets;
    private final Deque<BlockPos> queue = new ArrayDeque<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private int harvestedCount = 0;
    private int particleCounter = 0;

    public CascadeJob(ServerPlayer player, ServerLevel level, BlockPos start, int chainMax, int spellLevel) {
        this.player = player;
        this.level = level;
        this.chainMax = chainMax;
        this.additionalNeighborOffsets = buildAdditionalNeighborOffsets(spellLevel);
        BlockPos startPos = start.immutable();
        this.queue.add(startPos);
        this.visited.add(startPos);
    }

    private static int[][] buildAdditionalNeighborOffsets(int spellLevel) {
        if (spellLevel >= 3) {
            int[][] combined = new int[HORIZONTAL_DIAGONAL_OFFSETS.length + VERTICAL_EDGE_DIAGONAL_OFFSETS.length][];
            System.arraycopy(HORIZONTAL_DIAGONAL_OFFSETS, 0, combined, 0, HORIZONTAL_DIAGONAL_OFFSETS.length);
            System.arraycopy(VERTICAL_EDGE_DIAGONAL_OFFSETS, 0, combined, HORIZONTAL_DIAGONAL_OFFSETS.length, VERTICAL_EDGE_DIAGONAL_OFFSETS.length);
            return combined;
        }
        if (spellLevel >= 2) {
            return HORIZONTAL_DIAGONAL_OFFSETS;
        }
        return NO_ADDITIONAL_OFFSETS;
    }

    public boolean processWave() {
        if (!isPlayerStillValid()) {
            return false;
        }
        int cropsThisWave = Math.max(1, (chainMax + TARGET_WAVE_COUNT - 1) / TARGET_WAVE_COUNT);
        for (int i = 0; i < cropsThisWave && !isDone(); i++) {
            processOne();
        }
        return !isDone();
    }

    private boolean isDone() {
        return queue.isEmpty() || harvestedCount >= chainMax;
    }

    private boolean isPlayerStillValid() {
        return player.isAlive() && !player.hasDisconnected() && player.serverLevel() == level;
    }

    private void processOne() {
        if (isDone()) {
            return;
        }
        BlockPos pos = queue.poll();
        if (pos == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!CropTargeting.isHarvestable(state)) {
            return;
        }
        if (harvestAndReplant(pos, state)) {
            harvestedCount++;
            enqueueNeighbors(pos);
        }
    }

    private boolean harvestAndReplant(BlockPos pos, BlockState state) {
        Block cropBlock = state.getBlock();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);

        if (!fireBreakAllowed(pos, state)) {
            return false;
        }

        ItemStack seedStack = findSeedStack(drops, cropBlock);
        if (seedStack.isEmpty()) {
            level.removeBlock(pos, false);
            spawnBlockBreakEffects(pos, state);
            deliverDrops(drops);
            maybeSpawnTrailParticles(pos);
            return true;
        }

        seedStack.shrink(1);

        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        BlockState freshState = CropTargeting.newlyPlantedStatePreservingProperties(state);
        level.setBlock(pos, freshState, Block.UPDATE_ALL);

        BlockState placedAgainst = level.getBlockState(pos.below());
        BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(snapshot, placedAgainst, player);
        NeoForge.EVENT_BUS.post(placeEvent);
        if (placeEvent.isCanceled()) {
            snapshot.restore(Block.UPDATE_ALL);
            return false;
        }

        spawnBlockBreakEffects(pos, state);
        deliverDrops(drops);
        maybeSpawnTrailParticles(pos);
        return true;
    }

    private ItemStack findSeedStack(List<ItemStack> drops, Block cropBlock) {
        for (ItemStack drop : drops) {
            if (drop.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == cropBlock) {
                return drop;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean fireBreakAllowed(BlockPos pos, BlockState state) {
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, pos, state, player);
        NeoForge.EVENT_BUS.post(breakEvent);
        return !breakEvent.isCanceled();
    }

    private void spawnBlockBreakEffects(BlockPos pos, BlockState state) {
        level.levelEvent(LEVEL_EVENT_BLOCK_BREAK_PARTICLES_AND_SOUND, pos, Block.getId(state));
    }

    private void deliverDrops(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack copy = drop.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
    }

    private void maybeSpawnTrailParticles(BlockPos pos) {
        if (++particleCounter % PARTICLE_FREQUENCY_DIVISOR != 0) {
            return;
        }
        spawnTrailParticles(pos);
    }

    private void enqueueNeighbors(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            enqueueIfHarvestableCrop(pos.relative(direction));
        }
        for (int[] offset : additionalNeighborOffsets) {
            enqueueIfHarvestableCrop(pos.offset(offset[0], offset[1], offset[2]));
        }
    }

    private void enqueueIfHarvestableCrop(BlockPos neighbor) {
        if (!visited.add(neighbor)) {
            return;
        }
        if (CropTargeting.isHarvestable(level.getBlockState(neighbor))) {
            queue.add(neighbor);
        }
    }

    private void spawnTrailParticles(BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.COMPOSTER, cx, cy, cz, HARVEST_BURST_PARTICLE_COUNT, 0.3, 0.3, 0.3, 0.1);
        double dx = player.getX() - cx;
        double dy = player.getY() + player.getBbHeight() * 0.5 - cy;
        double dz = player.getZ() - cz;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int segments = Math.max(3, (int) (distance * TRAIL_PARTICLE_DENSITY));
        for (int i = 1; i <= segments; i++) {
            double t = (double) i / (segments + 1);
            double px = cx + dx * t;
            double py = cy + dy * t;
            double pz = cz + dz * t;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
