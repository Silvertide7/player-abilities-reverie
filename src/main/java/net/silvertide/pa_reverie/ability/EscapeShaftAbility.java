package net.silvertide.pa_reverie.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.silvertide.pa_reverie.entity.EscapeShaftRiseEntity;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.ArrayList;
import java.util.List;

public final class EscapeShaftAbility extends HarvestAbility {
    private static final int[] COOLDOWN_SECONDS_BY_LEVEL = {7200, 5400, 3600};
    private static final int BASE_SPELL_POWER = 5;
    private static final int SPELL_POWER_PER_LEVEL = 5;
    private static final double MIN_RISE_SPEED = 5.0;
    private static final double MAX_RISE_SPEED = 30.0;
    private static final int RISE_TARGET_OFFSET_ABOVE_SURFACE = 1;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return byLevel(level, 200, 160, 100);
    }

    @Override
    public int getCooldownTicks(int level) {
        return byLevel(level, COOLDOWN_SECONDS_BY_LEVEL) * TICKS_PER_SECOND;
    }

    @Override
    public boolean requiresStationary() {
        return true;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        if (player.level().dimension() == Level.NETHER) {
            AbilityAPI.setUseData(player, "message.pa_reverie.escape_shaft_wrong_dimension");
            return false;
        }
        if (player.getY() > 0.0) {
            AbilityAPI.setUseData(player, "message.pa_reverie.escape_shaft_too_shallow");
            return false;
        }
        if (player.serverLevel().canSeeSky(player.blockPosition())) {
            AbilityAPI.setUseData(player, "message.pa_reverie.escape_shaft_sky_visible");
            return false;
        }
        int surfaceY = player.serverLevel().getHeight(Heightmap.Types.OCEAN_FLOOR, player.getBlockX(), player.getBlockZ());
        int riseTargetY = surfaceY + RISE_TARGET_OFFSET_ABOVE_SURFACE;
        if (riseTargetY - player.getY() < 2) {
            AbilityAPI.setUseData(player, "message.pa_reverie.escape_shaft_no_sky");
            return false;
        }
        return true;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return AbilityAPI.getUseData(player) instanceof String failureKey
                ? Component.translatable(failureKey)
                : Component.translatable("message.pa_reverie.escape_shaft_no_sky");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        double blocksPerTick = riseSpeedBlocksPerSecond(player, level) / 20.0;
        List<ServerPlayer> participants = new ArrayList<>();
        participants.add(player);
        if (level >= getMaxLevel()) {
            participants.addAll(EscapeShaftRiseEntity.findCapturablePlayers(player.serverLevel(), player));
        }
        EscapeShaftRiseEntity.startRise(player.serverLevel(), participants, blocksPerTick, RISE_TARGET_OFFSET_ABOVE_SURFACE);
    }

    private double riseSpeedBlocksPerSecond(ServerPlayer player, int level) {
        return Math.clamp((double) spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level),
                MIN_RISE_SPEED, MAX_RISE_SPEED);
    }
}
