package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.silvertide.pa_reverie.support.ExcavateJob;
import net.silvertide.pa_reverie.support.ExcavateVolume;
import net.silvertide.player_abilities.api.AbilityTickJobs;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class ExcavateAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 2400;
    private static final int BASE_SPELL_POWER = 1;
    private static final int SPELL_POWER_PER_LEVEL = 0;
    private static final int TELEPORT_DROPS_MIN_LEVEL = 3;

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
        return !computeVolume(player, level).isEmpty();
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
        List<BlockPos> positions = computeVolume(player, level);
        if (positions.isEmpty()) {
            return;
        }
        ExcavateJob job = new ExcavateJob(player, player.serverLevel(), positions, level >= TELEPORT_DROPS_MIN_LEVEL);
        AbilityTickJobs.schedule(player, ExcavateJob.PROCESS_INTERVAL_TICKS, serverPlayer -> job.processWave());
    }

    private List<BlockPos> computeVolume(ServerPlayer player, int level) {
        int levelIndex = Math.clamp(level, 1, getMaxLevel()) - 1;
        int spellPowerBonus = ExcavateVolume.bonusForSpellPower(
                spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level));
        int depth = ExcavateVolume.depthFor(levelIndex, spellPowerBonus);
        int halfWidth = ExcavateVolume.halfWidthFor(levelIndex, spellPowerBonus);
        int height = ExcavateVolume.heightFor(levelIndex, spellPowerBonus);
        return ExcavateVolume.collect(player.serverLevel(), player, depth, halfWidth, height);
    }
}
