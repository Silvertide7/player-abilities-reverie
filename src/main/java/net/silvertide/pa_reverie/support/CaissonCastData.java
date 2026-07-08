package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;

public class CaissonCastData {

    private BlockPos center;
    private int elapsedTicks;
    private int nextOffsetIndex;

    public boolean isInitialized() {
        return center != null;
    }

    public void initialize(BlockPos center) {
        this.center = center;
    }

    public BlockPos getCenter() {
        return center;
    }

    public int incrementElapsedTicks() {
        return ++elapsedTicks;
    }

    public int getElapsedTicks() {
        return elapsedTicks;
    }

    public int getNextOffsetIndex() {
        return nextOffsetIndex;
    }

    public void advanceTo(int index) {
        this.nextOffsetIndex = index;
    }

    public void reset() {
        center = null;
        elapsedTicks = 0;
        nextOffsetIndex = 0;
    }
}
