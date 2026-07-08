package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;

public class VerdantCascadeCastData {

    private BlockPos lockedTarget;

    public BlockPos getLockedTarget() {
        return lockedTarget;
    }

    public void setLockedTarget(BlockPos target) {
        this.lockedTarget = target;
    }

    public boolean hasLockedTarget() {
        return lockedTarget != null;
    }

    public void reset() {
        lockedTarget = null;
    }
}
