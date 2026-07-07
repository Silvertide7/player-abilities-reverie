package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class WoodsongCastData {

    private int lastScannedRadius = 0;
    private final Set<BlockPos> visitedLogs = new HashSet<>();
    private final Set<BlockPos> foundTreeRoots = new HashSet<>();
    private final Map<Block, Integer> foundTreesByLogType = new HashMap<>();

    public int getLastScannedRadius() {
        return lastScannedRadius;
    }

    public void setLastScannedRadius(int radius) {
        this.lastScannedRadius = radius;
    }

    public boolean isLogVisited(BlockPos pos) {
        return visitedLogs.contains(pos);
    }

    public void markLogsVisited(Collection<BlockPos> positions) {
        visitedLogs.addAll(positions);
    }

    public boolean recordFoundTree(BlockPos root, Block logType) {
        if (!foundTreeRoots.add(root)) {
            return false;
        }
        foundTreesByLogType.merge(logType, 1, Integer::sum);
        return true;
    }

    public int totalTreesFound() {
        return foundTreeRoots.size();
    }

    public boolean hasFoundAnyTrees() {
        return !foundTreesByLogType.isEmpty();
    }

    public void forEachFoundTree(BiConsumer<Block, Integer> consumer) {
        foundTreesByLogType.forEach(consumer);
    }

}
