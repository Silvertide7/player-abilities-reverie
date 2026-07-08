package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.pa_reverie.network.TremorSenseHighlightPacket;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.pa_reverie.support.TremorScanner;
import net.silvertide.pa_reverie.support.TremorSenseCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class TremorSenseAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 2400;
    private static final int BASE_SPELL_POWER = 10;
    private static final int SPELL_POWER_PER_LEVEL = 5;
    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS_CAP = 40;
    private static final int[] MAX_HIGHLIGHTED_POSITIONS_BY_LEVEL = {60, 180, 256};
    private static final int HIGHLIGHT_DURATION_SECONDS = 40;
    private static final int LEVEL_THAT_ENABLES_TIER_COLORS = 3;
    private static final int DARKNESS_REFRESH_DURATION_TICKS = 100;
    private static final int DARKNESS_REFRESH_INTERVAL_TICKS = 40;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 160;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        ServerLevel serverLevel = player.serverLevel();
        TremorSenseCastData castData;
        boolean isFirstCastTick;
        if (AbilityAPI.getUseData(player) instanceof TremorSenseCastData existing) {
            castData = existing;
            isFirstCastTick = false;
        } else {
            castData = new TremorSenseCastData(maxHighlightedPositionsForLevel(player, level));
            AbilityAPI.setUseData(player, castData);
            isFirstCastTick = true;
        }
        float chargeFraction = Math.min(1.0f, elapsedTicks / (float) totalTicks);
        int maxRadius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS_CAP,
                Math.round(spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level))));
        int targetRadius = MIN_RADIUS + Math.round(chargeFraction * (maxRadius - MIN_RADIUS));
        int lastScanned = castData.getLastScannedRadius();
        if (!castData.isFull() && targetRadius > lastScanned) {
            BlockPos center = player.blockPosition();
            int startRadius = Math.max(lastScanned + 1, MIN_RADIUS);
            for (int radius = startRadius; radius <= targetRadius; radius++) {
                TremorScanner.scanShell(serverLevel, center, radius, castData);
                if (castData.isFull()) {
                    break;
                }
                castData.setLastScannedRadius(radius);
            }
        }
        if (isFirstCastTick || player.tickCount % DARKNESS_REFRESH_INTERVAL_TICKS == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
                    DARKNESS_REFRESH_DURATION_TICKS, 0, false, false, false));
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (!(AbilityAPI.getUseData(player) instanceof TremorSenseCastData castData) || !castData.hasAnyFound()) {
            return;
        }
        boolean useTierColors = level >= LEVEL_THAT_ENABLES_TIER_COLORS;
        PacketDistributor.sendToPlayer(player, new TremorSenseHighlightPacket(
                player.blockPosition(), castData.getFoundPositions(),
                HIGHLIGHT_DURATION_SECONDS * 20, useTierColors));
        player.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                40, 0.6, 0.6, 0.6, 0.1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.6f, 1.8f);
    }

    private int maxHighlightedPositionsForLevel(ServerPlayer player, int level) {
        return ReverieMagicAttributes.scaledByHarvestPower(player,
                MAX_HIGHLIGHTED_POSITIONS_BY_LEVEL[Math.clamp(level, 1, getMaxLevel()) - 1]);
    }
}
