package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.silvertide.pa_reverie.PAReverie;

public final class TremorScanner {

    public static final TagKey<Block> TARGETS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "tremor_sense_targets")
    );

    private TremorScanner() {}

    public static void scanShell(ServerLevel level, BlockPos center, int radius, TremorSenseCastData castData) {
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

    private static void checkPos(ServerLevel level, BlockPos.MutableBlockPos cursor, TremorSenseCastData castData) {
        if (castData.isFull()) {
            return;
        }
        if (cursor.getY() < level.getMinBuildHeight() || cursor.getY() >= level.getMaxBuildHeight()) {
            return;
        }
        if (!level.getChunkSource().hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4)) {
            return;
        }
        BlockState state = level.getBlockState(cursor);
        if (!state.is(TARGETS)) {
            return;
        }
        castData.addFoundPosition(cursor.immutable());
    }
}
