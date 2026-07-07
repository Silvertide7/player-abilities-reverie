package net.silvertide.pa_reverie.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.silvertide.pa_reverie.network.TremorSenseHighlightPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TremorSenseRenderState {

    public record HighlightedBlock(BlockPos pos, float distanceFromOrigin, int color) {}

    public record ActiveHighlight(
            BlockPos origin,
            List<HighlightedBlock> blocks,
            long startGameTick,
            long endGameTick,
            float maxDistanceFromOrigin
    ) {}

    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xFFD580;

    private static final Map<Block, Integer> ORE_TIER_COLORS = Map.ofEntries(
            Map.entry(Blocks.COAL_ORE, 0x4A4A4A),
            Map.entry(Blocks.DEEPSLATE_COAL_ORE, 0x4A4A4A),
            Map.entry(Blocks.IRON_ORE, 0xD8AF93),
            Map.entry(Blocks.DEEPSLATE_IRON_ORE, 0xD8AF93),
            Map.entry(Blocks.COPPER_ORE, 0xB87333),
            Map.entry(Blocks.DEEPSLATE_COPPER_ORE, 0xB87333),
            Map.entry(Blocks.GOLD_ORE, 0xFCEE4B),
            Map.entry(Blocks.DEEPSLATE_GOLD_ORE, 0xFCEE4B),
            Map.entry(Blocks.NETHER_GOLD_ORE, 0xFCEE4B),
            Map.entry(Blocks.REDSTONE_ORE, 0xFF4D4D),
            Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, 0xFF4D4D),
            Map.entry(Blocks.LAPIS_ORE, 0x4D7AFF),
            Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, 0x4D7AFF),
            Map.entry(Blocks.DIAMOND_ORE, 0x4DFFFA),
            Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, 0x4DFFFA),
            Map.entry(Blocks.EMERALD_ORE, 0x33FF66),
            Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, 0x33FF66),
            Map.entry(Blocks.NETHER_QUARTZ_ORE, 0xF5DDD5),
            Map.entry(Blocks.ANCIENT_DEBRIS, 0xC04640),
            Map.entry(Blocks.SUSPICIOUS_SAND, 0xC8A2C8),
            Map.entry(Blocks.SUSPICIOUS_GRAVEL, 0xB39EBF)
    );

    private static ActiveHighlight active;

    private TremorSenseRenderState() {}

    public static void install(TremorSenseHighlightPacket packet) {
        long now = currentGameTick();
        if (now < 0) {
            return;
        }
        BlockPos origin = packet.origin();
        List<HighlightedBlock> blocks = new ArrayList<>(packet.positions().size());
        float maxDistance = 0f;
        Minecraft minecraft = Minecraft.getInstance();
        for (BlockPos pos : packet.positions()) {
            float dx = pos.getX() + 0.5f - (origin.getX() + 0.5f);
            float dy = pos.getY() + 0.5f - (origin.getY() + 0.5f);
            float dz = pos.getZ() + 0.5f - (origin.getZ() + 0.5f);
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > maxDistance) {
                maxDistance = distance;
            }
            int color = resolveColor(packet.useTierColors(), pos, minecraft);
            blocks.add(new HighlightedBlock(pos.immutable(), distance, color));
        }
        active = new ActiveHighlight(origin, blocks, now, now + packet.durationTicks(), Math.max(1f, maxDistance));
    }

    public static ActiveHighlight current() {
        if (active == null) {
            return null;
        }
        if (currentGameTick() >= active.endGameTick) {
            active = null;
            return null;
        }
        return active;
    }

    public static void clear() {
        active = null;
    }

    private static int resolveColor(boolean useTierColors, BlockPos pos, Minecraft minecraft) {
        if (!useTierColors) {
            return DEFAULT_HIGHLIGHT_COLOR;
        }
        if (minecraft.level == null) {
            return DEFAULT_HIGHLIGHT_COLOR;
        }
        Block block = minecraft.level.getBlockState(pos).getBlock();
        return ORE_TIER_COLORS.getOrDefault(block, DEFAULT_HIGHLIGHT_COLOR);
    }

    private static long currentGameTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return -1L;
        }
        return minecraft.level.getGameTime();
    }
}
