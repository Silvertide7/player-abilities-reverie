package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.silvertide.pa_reverie.support.ExcavateVolume;
import net.silvertide.player_abilities.api.AbilityTickJobs;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class ExcavateAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 2400;
    private static final int WAVE_INTERVAL_TICKS = 4;
    private static final int TARGET_WAVE_COUNT = 10;
    private static final int MAX_BLOCKS_PER_WAVE = 16;

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
        return !collectVolume(player, level).isEmpty();
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.excavate_nothing_to_dig");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        List<BlockPos> positions = collectVolume(player, level);
        if (positions.isEmpty()) {
            return;
        }
        boolean teleportDropsToPlayer = level >= getMaxLevel();
        int blocksPerWave = Math.min(MAX_BLOCKS_PER_WAVE,
                Math.max(1, (positions.size() + TARGET_WAVE_COUNT - 1) / TARGET_WAVE_COUNT));
        int[] nextIndex = {0};
        AbilityTickJobs.schedule(player, WAVE_INTERVAL_TICKS, jobPlayer -> {
            for (int processed = 0; processed < blocksPerWave && nextIndex[0] < positions.size(); processed++) {
                excavateBlock(jobPlayer, positions.get(nextIndex[0]++), teleportDropsToPlayer);
            }
            return nextIndex[0] < positions.size();
        });
    }

    private static void excavateBlock(ServerPlayer player, BlockPos pos, boolean teleportDropsToPlayer) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);
        if (!ExcavateVolume.isExcavatable(level, pos)) {
            return;
        }
        if (CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, state).isCanceled()) {
            return;
        }
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
        level.removeBlock(pos, false);
        level.levelEvent(2001, pos, Block.getId(state));
        for (ItemStack drop : drops) {
            if (teleportDropsToPlayer) {
                if (!player.getInventory().add(drop)) {
                    Block.popResource(level, player.blockPosition(), drop);
                }
            } else {
                Block.popResource(level, pos, drop);
            }
        }
    }

    private List<BlockPos> collectVolume(ServerPlayer player, int level) {
        int levelIndex = level - 1;
        int spellPowerBonus = ExcavateVolume.bonusForSpellPower(
                (float) AbilityAPI.getAbilityPower(player));
        return ExcavateVolume.collect(player.serverLevel(), player,
                ExcavateVolume.depthFor(levelIndex, spellPowerBonus),
                ExcavateVolume.halfWidthFor(levelIndex, spellPowerBonus),
                ExcavateVolume.heightFor(levelIndex, spellPowerBonus));
    }
}
