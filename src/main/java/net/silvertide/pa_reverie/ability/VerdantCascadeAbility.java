package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.silvertide.pa_reverie.support.CropTargeting;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityTickJobs;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class VerdantCascadeAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 3600;
    private static final double TARGET_REACH = 8.0;
    private static final int WAVE_INTERVAL_TICKS = 4;
    private static final int TARGET_WAVE_COUNT = 50;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 40;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        BlockPos origin = raycastCrop(player);
        AbilityAPI.setUseData(player, origin);
        return origin != null;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.verdant_cascade_no_crop");
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (!(AbilityAPI.getUseData(player) instanceof BlockPos origin)) {
            return;
        }
        int chainMax = Mth.clamp(byLevel(level, 100, 200, 300), 100, 600);
        int cropsPerWave = Math.max(1, (chainMax + TARGET_WAVE_COUNT - 1) / TARGET_WAVE_COUNT);
        List<BlockPos> neighborOffsets = neighborOffsetsForLevel(level);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);
        int[] harvested = {0};
        AbilityTickJobs.schedule(player, WAVE_INTERVAL_TICKS, jobPlayer -> {
            for (int processed = 0; processed < cropsPerWave; processed++) {
                BlockPos crop = queue.poll();
                if (crop == null || harvested[0] >= chainMax) {
                    return false;
                }
                if (harvestAndReplant(jobPlayer, crop)) {
                    harvested[0]++;
                    for (BlockPos offset : neighborOffsets) {
                        BlockPos neighbor = crop.offset(offset.getX(), offset.getY(), offset.getZ());
                        if (visited.add(neighbor)
                                && CropTargeting.isHarvestable(jobPlayer.serverLevel().getBlockState(neighbor))) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
            return !queue.isEmpty() && harvested[0] < chainMax;
        });
    }

    private static boolean harvestAndReplant(ServerPlayer player, BlockPos crop) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(crop);
        if (!CropTargeting.isHarvestable(state)) {
            return false;
        }
        if (CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, crop, state).isCanceled()) {
            return false;
        }
        List<ItemStack> drops = new ArrayList<>(
                Block.getDrops(state, level, crop, level.getBlockEntity(crop), player, player.getMainHandItem()));
        ItemStack seed = findSeed(drops, state);
        if (!seed.isEmpty()) {
            seed.shrink(1);
            level.setBlock(crop, CropTargeting.newlyPlantedStatePreservingProperties(state), 3);
        } else {
            level.removeBlock(crop, false);
        }
        level.levelEvent(2001, crop, Block.getId(state));
        if (player.getRandom().nextInt(3) == 0) {
            level.sendParticles(ParticleTypes.COMPOSTER,
                    crop.getX() + 0.5, crop.getY() + 0.5, crop.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.05);
        }
        for (ItemStack drop : drops) {
            if (!drop.isEmpty() && !player.getInventory().add(drop)) {
                Block.popResource(level, player.blockPosition(), drop);
            }
        }
        return true;
    }

    private static ItemStack findSeed(List<ItemStack> drops, BlockState cropState) {
        Block cropBlock = cropState.getBlock();
        for (ItemStack drop : drops) {
            if (drop.getItem() == cropBlock.asItem()
                    || (drop.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                    && blockItem.getBlock() == cropBlock)
                    || (drop.getItem() instanceof net.minecraft.world.item.ItemNameBlockItem nameBlockItem
                    && nameBlockItem.getBlock() == cropBlock)) {
                return drop;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<BlockPos> neighborOffsetsForLevel(int level) {
        List<BlockPos> offsets = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            offsets.add(new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
        }
        if (level >= 2) {
            offsets.add(new BlockPos(1, 0, 1));
            offsets.add(new BlockPos(1, 0, -1));
            offsets.add(new BlockPos(-1, 0, 1));
            offsets.add(new BlockPos(-1, 0, -1));
        }
        if (level >= 3) {
            for (int dy : new int[]{-1, 1}) {
                offsets.add(new BlockPos(1, dy, 0));
                offsets.add(new BlockPos(-1, dy, 0));
                offsets.add(new BlockPos(0, dy, 1));
                offsets.add(new BlockPos(0, dy, -1));
            }
        }
        return offsets;
    }

    private static BlockPos raycastCrop(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 reachEnd = eyePosition.add(player.getViewVector(1.0f).scale(TARGET_REACH));
        BlockHitResult hit = player.level().clip(new ClipContext(eyePosition, reachEnd,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return CropTargeting.isHarvestable(player.level().getBlockState(hit.getBlockPos())) ? hit.getBlockPos() : null;
    }
}
