package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class CanopyScanner {

    public enum Result { SUCCESS, NOT_A_TREE, OUT_OF_REACH }

    public record CanopyTop(Result result, BlockPos top) {
        private static CanopyTop failure(Result result) {
            return new CanopyTop(result, null);
        }
    }

    private static final int CANOPY_GAP_TOLERANCE = 2;

    private CanopyScanner() {}

    public static CanopyTop findCanopyTop(Level level, BlockPos hit, int climbCap) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int startY = hit.getY();
        int maxY = Math.min(startY + climbCap, level.getMaxBuildHeight() - 1);
        boolean sawNaturalLeaf = false;
        boolean canopyEnded = false;
        BlockPos highest = hit.immutable();
        int consecutiveAir = 0;
        for (int y = startY; y <= maxY; y++) {
            cursor.set(hit.getX(), y, hit.getZ());
            BlockState state = level.getBlockState(cursor);
            if (state.is(BlockTags.LOGS)) {
                highest = cursor.immutable();
                consecutiveAir = 0;
            } else if (state.is(BlockTags.LEAVES)) {
                highest = cursor.immutable();
                consecutiveAir = 0;
                if (isNaturalLeaf(state)) {
                    sawNaturalLeaf = true;
                }
            } else {
                consecutiveAir++;
                if (consecutiveAir > CANOPY_GAP_TOLERANCE) {
                    canopyEnded = true;
                    break;
                }
            }
        }
        if (!canopyEnded) {
            return CanopyTop.failure(Result.OUT_OF_REACH);
        }
        if (!sawNaturalLeaf) {
            return CanopyTop.failure(Result.NOT_A_TREE);
        }
        return new CanopyTop(Result.SUCCESS, highest);
    }

    private static boolean isNaturalLeaf(BlockState state) {
        if (!state.hasProperty(BlockStateProperties.PERSISTENT)) {
            return true;
        }
        return !state.getValue(BlockStateProperties.PERSISTENT);
    }
}
