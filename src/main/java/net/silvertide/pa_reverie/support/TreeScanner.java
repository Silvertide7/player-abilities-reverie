package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class TreeScanner {

    private static final int LOG_CLUSTER_MAX_SIZE = 64;
    private static final int LEAF_SEARCH_RADIUS = 2;
    private static final int MIN_NON_PERSISTENT_LEAVES = 4;

    private static final int DISCOVERY_PARTICLE_COUNT = 6;
    private static final float CHIME_BASE_PITCH = 0.9f;
    private static final float CHIME_PITCH_STEP = 0.08f;
    private static final float CHIME_PITCH_CEILING = 1.6f;
    private static final float CHIME_VOLUME = 0.5f;

    private TreeScanner() {}

    public static void scanShell(ServerLevel level, BlockPos center, int radius, WoodsongCastData castData) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                checkPos(level, cursor.set(center.getX() + dx, center.getY() + radius, center.getZ() + dz), castData);
                checkPos(level, cursor.set(center.getX() + dx, center.getY() - radius, center.getZ() + dz), castData);
            }
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius + 1; dy <= radius - 1; dy++) {
                checkPos(level, cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + radius), castData);
                checkPos(level, cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() - radius), castData);
            }
        }
        for (int dy = -radius + 1; dy <= radius - 1; dy++) {
            for (int dz = -radius + 1; dz <= radius - 1; dz++) {
                checkPos(level, cursor.set(center.getX() + radius, center.getY() + dy, center.getZ() + dz), castData);
                checkPos(level, cursor.set(center.getX() - radius, center.getY() + dy, center.getZ() + dz), castData);
            }
        }
    }

    private static void checkPos(ServerLevel level, BlockPos.MutableBlockPos cursor, WoodsongCastData castData) {
        BlockState state = level.getBlockState(cursor);
        if (!state.is(BlockTags.LOGS)) {
            return;
        }
        BlockPos logPos = cursor.immutable();
        if (castData.isLogVisited(logPos)) {
            return;
        }
        Block logType = state.getBlock();
        Set<BlockPos> cluster = bfsLogCluster(level, logPos, logType);
        castData.markLogsVisited(cluster);
        if (!TreeYields.isSupportedLog(logType)) {
            return;
        }
        if (!isValidGrownTree(level, cluster)) {
            return;
        }
        BlockPos root = findRootLog(cluster);
        if (castData.recordFoundTree(root, logType)) {
            spawnDiscoveryFeedback(level, root, castData.totalTreesFound());
        }
    }

    private static Set<BlockPos> bfsLogCluster(ServerLevel level, BlockPos start, Block matchType) {
        Set<BlockPos> cluster = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        cluster.add(start);
        while (!queue.isEmpty() && cluster.size() < LOG_CLUSTER_MAX_SIZE) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (cluster.contains(neighbor)) {
                    continue;
                }
                if (level.getBlockState(neighbor).getBlock() == matchType) {
                    cluster.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return cluster;
    }

    private static boolean isValidGrownTree(ServerLevel level, Set<BlockPos> cluster) {
        int nonPersistentLeafCount = 0;
        Set<BlockPos> inspectedLeafPositions = new HashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos logPos : cluster) {
            for (int dx = -LEAF_SEARCH_RADIUS; dx <= LEAF_SEARCH_RADIUS; dx++) {
                for (int dy = -LEAF_SEARCH_RADIUS; dy <= LEAF_SEARCH_RADIUS; dy++) {
                    for (int dz = -LEAF_SEARCH_RADIUS; dz <= LEAF_SEARCH_RADIUS; dz++) {
                        cursor.set(logPos.getX() + dx, logPos.getY() + dy, logPos.getZ() + dz);
                        BlockState leafState = level.getBlockState(cursor);
                        if (!leafState.is(BlockTags.LEAVES)
                                || !leafState.hasProperty(LeavesBlock.PERSISTENT)
                                || leafState.getValue(LeavesBlock.PERSISTENT)) {
                            continue;
                        }
                        BlockPos leafPos = cursor.immutable();
                        if (!inspectedLeafPositions.add(leafPos)) {
                            continue;
                        }
                        nonPersistentLeafCount++;
                        if (nonPersistentLeafCount >= MIN_NON_PERSISTENT_LEAVES) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static BlockPos findRootLog(Set<BlockPos> cluster) {
        BlockPos root = null;
        int minY = Integer.MAX_VALUE;
        for (BlockPos pos : cluster) {
            if (pos.getY() < minY) {
                minY = pos.getY();
                root = pos;
            }
        }
        return root;
    }

    private static void spawnDiscoveryFeedback(ServerLevel level, BlockPos treeRoot, int treesFoundSoFar) {
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                treeRoot.getX() + 0.5,
                treeRoot.getY() + 0.5,
                treeRoot.getZ() + 0.5,
                DISCOVERY_PARTICLE_COUNT,
                0.4, 0.6, 0.4,
                0.0
        );
        float pitch = Math.min(CHIME_PITCH_CEILING, CHIME_BASE_PITCH + (treesFoundSoFar - 1) * CHIME_PITCH_STEP);
        level.playSound(
                null,
                treeRoot.getX() + 0.5,
                treeRoot.getY() + 0.5,
                treeRoot.getZ() + 0.5,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundSource.PLAYERS,
                CHIME_VOLUME,
                pitch
        );
    }
}
