package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExcavateVolume {

    private static final int[] BASE_DEPTH_BY_LEVEL = { 5, 7, 9 };
    private static final int[] BASE_HALF_WIDTH_BY_LEVEL = { 2, 3, 4 };
    private static final int[] BASE_HEIGHT_BY_LEVEL = { 4, 5, 6 };

    private static final double SPELL_POWER_BASELINE = 1.0;
    private static final double SPELL_POWER_BONUS_SLOPE = 2.0;
    private static final int MAX_SPELL_POWER_BONUS = 3;

    private static final int FORWARD_START_OFFSET = 1;

    private ExcavateVolume() {}

    public static int bonusForSpellPower(float spellPower) {
        return Math.clamp(Math.round((spellPower - SPELL_POWER_BASELINE) * SPELL_POWER_BONUS_SLOPE), 0, MAX_SPELL_POWER_BONUS);
    }

    public static int depthFor(int levelIndex, int spellPowerBonus) {
        return BASE_DEPTH_BY_LEVEL[levelIndex] + spellPowerBonus;
    }

    public static int halfWidthFor(int levelIndex, int spellPowerBonus) {
        return BASE_HALF_WIDTH_BY_LEVEL[levelIndex] + spellPowerBonus;
    }

    public static int heightFor(int levelIndex, int spellPowerBonus) {
        return BASE_HEIGHT_BY_LEVEL[levelIndex] + spellPowerBonus;
    }

    public static List<BlockPos> collect(ServerLevel level, LivingEntity caster, int depth, int halfWidth, int height) {
        BlockPos feet = caster.blockPosition();
        Direction forward = caster.getDirection();
        Direction right = forward.getClockWise();
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int forwardOffset = FORWARD_START_OFFSET; forwardOffset < FORWARD_START_OFFSET + depth; forwardOffset++) {
            for (int lateralOffset = -halfWidth; lateralOffset <= halfWidth; lateralOffset++) {
                for (int verticalOffset = 0; verticalOffset < height; verticalOffset++) {
                    int x = feet.getX() + forward.getStepX() * forwardOffset + right.getStepX() * lateralOffset;
                    int y = feet.getY() + verticalOffset;
                    int z = feet.getZ() + forward.getStepZ() * forwardOffset + right.getStepZ() * lateralOffset;
                    cursor.set(x, y, z);
                    if (isExcavatable(level, cursor)) {
                        positions.add(cursor.immutable());
                    }
                }
            }
        }
        positions.sort(
                Comparator.<BlockPos>comparingInt(pos -> depthAlong(pos, feet, forward))
                        .thenComparingInt(pos -> Math.abs(lateralAlong(pos, feet, right)))
                        .thenComparingInt(pos -> -pos.getY()));
        return positions;
    }

    public static boolean isExcavatable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir()
                && state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                && state.getDestroySpeed(level, pos) >= 0;
    }

    private static int depthAlong(BlockPos pos, BlockPos anchor, Direction forward) {
        return (pos.getX() - anchor.getX()) * forward.getStepX() + (pos.getZ() - anchor.getZ()) * forward.getStepZ();
    }

    private static int lateralAlong(BlockPos pos, BlockPos anchor, Direction right) {
        return (pos.getX() - anchor.getX()) * right.getStepX() + (pos.getZ() - anchor.getZ()) * right.getStepZ();
    }
}
