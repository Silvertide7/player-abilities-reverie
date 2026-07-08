package net.silvertide.pa_reverie.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class EscapeShaftClientGhostShaft {

    private static final int CLIENT_RENDER_UPDATE_FLAGS =
            Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private static final int DEEPSLATE_TRANSITION_Y = 0;
    private static final int TOPSOIL_DEPTH_BLOCKS = 5;
    private static final float TOPSOIL_DIRT_CHANCE = 0.5f;

    private EscapeShaftClientGhostShaft() {}

    public static void applyShaft(BlockPos shaftMin, BlockPos shaftMax) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockState innerAir = Blocks.AIR.defaultBlockState();
        BlockState framePosts = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState deepWall = Blocks.DEEPSLATE.defaultBlockState();
        BlockState topsoilWall = Blocks.DIRT.defaultBlockState();
        BlockState stoneWall = Blocks.STONE.defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = shaftMin.getX(); x <= shaftMax.getX(); x++) {
            for (int z = shaftMin.getZ(); z <= shaftMax.getZ(); z++) {
                for (int y = shaftMin.getY(); y <= shaftMax.getY(); y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).isAir()) {
                        level.setBlock(cursor.immutable(), innerAir, CLIENT_RENDER_UPDATE_FLAGS);
                    }
                }
            }
        }
        int minX = shaftMin.getX();
        int maxX = shaftMax.getX();
        int minZ = shaftMin.getZ();
        int maxZ = shaftMax.getZ();
        int topY = shaftMax.getY();
        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int z = minZ - 1; z <= maxZ + 1; z++) {
                if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
                    continue;
                }
                boolean postColumn = isFramePost(x, z, minX, maxX, minZ, maxZ);
                for (int y = shaftMin.getY(); y <= shaftMax.getY(); y++) {
                    BlockState wall;
                    if (postColumn) {
                        wall = framePosts;
                    } else if (y < DEEPSLATE_TRANSITION_Y) {
                        wall = deepWall;
                    } else if (y > topY - TOPSOIL_DEPTH_BLOCKS && level.random.nextFloat() < TOPSOIL_DIRT_CHANCE) {
                        wall = topsoilWall;
                    } else {
                        wall = stoneWall;
                    }
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(wall.getBlock())) {
                        level.setBlock(cursor.immutable(), wall, CLIENT_RENDER_UPDATE_FLAGS);
                    }
                }
            }
        }
    }

    private static boolean isFramePost(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        boolean onXWall = x == minX - 1 || x == maxX + 1;
        boolean onZWall = z == minZ - 1 || z == maxZ + 1;
        boolean atCorner = onXWall && onZWall;
        boolean besideCornerAlongXWall = onXWall && (z == minZ || z == maxZ);
        boolean besideCornerAlongZWall = onZWall && (x == minX || x == maxX);
        return atCorner || besideCornerAlongXWall || besideCornerAlongZWall;
    }
}
