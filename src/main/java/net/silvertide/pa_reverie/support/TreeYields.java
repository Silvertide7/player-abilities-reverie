package net.silvertide.pa_reverie.support;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TreeYields {

    public record TreeYield(Block leaves, Item sapling) {}

    private static final String LOG_SUFFIX = "_log";
    private static final String LEAVES_SUFFIX = "_leaves";
    private static final String SAPLING_SUFFIX = "_sapling";

    private static final Map<Block, TreeYield> HARDCODED_YIELDS_BY_LOG = Map.ofEntries(
            Map.entry(Blocks.OAK_LOG, new TreeYield(Blocks.OAK_LEAVES, Items.OAK_SAPLING)),
            Map.entry(Blocks.SPRUCE_LOG, new TreeYield(Blocks.SPRUCE_LEAVES, Items.SPRUCE_SAPLING)),
            Map.entry(Blocks.BIRCH_LOG, new TreeYield(Blocks.BIRCH_LEAVES, Items.BIRCH_SAPLING)),
            Map.entry(Blocks.JUNGLE_LOG, new TreeYield(Blocks.JUNGLE_LEAVES, Items.JUNGLE_SAPLING)),
            Map.entry(Blocks.ACACIA_LOG, new TreeYield(Blocks.ACACIA_LEAVES, Items.ACACIA_SAPLING)),
            Map.entry(Blocks.DARK_OAK_LOG, new TreeYield(Blocks.DARK_OAK_LEAVES, Items.DARK_OAK_SAPLING)),
            Map.entry(Blocks.MANGROVE_LOG, new TreeYield(Blocks.MANGROVE_LEAVES, Items.MANGROVE_PROPAGULE)),
            Map.entry(Blocks.CHERRY_LOG, new TreeYield(Blocks.CHERRY_LEAVES, Items.CHERRY_SAPLING))
    );

    private static final Map<Block, Optional<TreeYield>> RESOLVED_LOG_CACHE = new ConcurrentHashMap<>();

    private TreeYields() {}

    @Nullable
    public static TreeYield lookup(Block log) {
        TreeYield hardcoded = HARDCODED_YIELDS_BY_LOG.get(log);
        if (hardcoded != null) {
            return hardcoded;
        }
        return RESOLVED_LOG_CACHE.computeIfAbsent(log, TreeYields::deriveYieldFromLogName).orElse(null);
    }

    public static boolean isSupportedLog(Block log) {
        return lookup(log) != null;
    }

    private static Optional<TreeYield> deriveYieldFromLogName(Block log) {
        ResourceLocation logId = BuiltInRegistries.BLOCK.getKey(log);
        String logPath = logId.getPath();
        if (!logPath.endsWith(LOG_SUFFIX)) {
            return Optional.empty();
        }
        String speciesPath = logPath.substring(0, logPath.length() - LOG_SUFFIX.length());
        if (speciesPath.isEmpty()) {
            return Optional.empty();
        }
        Block leavesBlock = BuiltInRegistries.BLOCK
                .getOptional(ResourceLocation.fromNamespaceAndPath(logId.getNamespace(), speciesPath + LEAVES_SUFFIX))
                .orElse(null);
        Item saplingItem = BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath(logId.getNamespace(), speciesPath + SAPLING_SUFFIX))
                .orElse(null);
        if (leavesBlock == null || leavesBlock == Blocks.AIR || saplingItem == null || saplingItem == Items.AIR) {
            return Optional.empty();
        }
        return Optional.of(new TreeYield(leavesBlock, saplingItem));
    }
}
