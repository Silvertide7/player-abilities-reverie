package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.silvertide.pa_reverie.registry.ReverieBlocks;
import net.silvertide.pa_reverie.support.SphereOffsets;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class CaissonAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 600;
    private static final int COLLAPSE_STAGGER_DIVISOR = 64;

    private record CaissonUseData(BlockPos center, List<Vec3i> offsets, int[] nextOffsetIndex) {
    }

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
        return player.isInWater();
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.caisson_no_water");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        int radius = Mth.clamp(
                (int) Math.round(byLevel(level, 5, 7, 9) * AbilityAPI.getAbilityPower(player)), 3, 12);
        AbilityAPI.setUseData(player, new CaissonUseData(player.blockPosition(),
                SphereOffsets.sortedOffsetsForRadius(radius), new int[]{0}));
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        player.setAirSupply(player.getMaxAirSupply());
        if (!(AbilityAPI.getUseData(player) instanceof CaissonUseData useData)) {
            return;
        }
        int targetIndex = Math.min(useData.offsets().size(),
                useData.offsets().size() * elapsedTicks / totalTicks);
        convertUpTo(player, level, useData, targetIndex, totalTicks - elapsedTicks);
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (AbilityAPI.getUseData(player) instanceof CaissonUseData useData) {
            convertUpTo(player, level, useData, useData.offsets().size(), 0);
        }
    }

    private void convertUpTo(ServerPlayer player, int level, CaissonUseData useData,
                             int targetIndex, int ticksUntilComplete) {
        ServerLevel serverLevel = player.serverLevel();
        int lifetimeTicks = (int) Math.round(byLevel(level, 600, 1200, 1800)
                * Math.min(2.0, AbilityAPI.getAbilityPower(player)));
        int outermostIndex = useData.offsets().size() - 1;
        while (useData.nextOffsetIndex()[0] < targetIndex) {
            int index = useData.nextOffsetIndex()[0]++;
            BlockPos target = useData.center().offset(useData.offsets().get(index));
            if (!serverLevel.getBlockState(target).is(Blocks.WATER)) {
                continue;
            }
            serverLevel.setBlock(target, ReverieBlocks.DRY_AIR.get().defaultBlockState(), 3);
            int collapseStagger = (outermostIndex - index) / COLLAPSE_STAGGER_DIVISOR;
            serverLevel.scheduleTick(target, ReverieBlocks.DRY_AIR.get(),
                    ticksUntilComplete + lifetimeTicks + collapseStagger);
        }
    }
}
