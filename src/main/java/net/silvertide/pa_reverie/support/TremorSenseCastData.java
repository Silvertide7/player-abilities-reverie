package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TremorSenseCastData {

    private final int maxFoundPositions;
    private int lastScannedRadius = 0;
    private final List<BlockPos> foundPositions = new ArrayList<>();

    public TremorSenseCastData(int maxFoundPositions) {
        this.maxFoundPositions = maxFoundPositions;
    }

    public int getLastScannedRadius() {
        return lastScannedRadius;
    }

    public void setLastScannedRadius(int radius) {
        this.lastScannedRadius = radius;
    }

    public void addFoundPosition(BlockPos pos) {
        if (foundPositions.size() >= maxFoundPositions) {
            return;
        }
        foundPositions.add(pos);
    }

    public boolean isFull() {
        return foundPositions.size() >= maxFoundPositions;
    }

    public List<BlockPos> getFoundPositions() {
        return List.copyOf(foundPositions);
    }

    public boolean hasAnyFound() {
        return !foundPositions.isEmpty();
    }

}
