package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.pa_reverie.network.TremorSenseHighlightPacket;
import net.silvertide.pa_reverie.support.TremorScanner;
import net.silvertide.pa_reverie.support.TremorSenseCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class TremorSenseAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 2400;
    private static final int MIN_SCAN_RADIUS = 4;
    private static final int DARKNESS_REFRESH_INTERVAL_TICKS = 40;
    private static final int HIGHLIGHT_DURATION_TICKS = 800;

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
        AbilityAPI.setUseData(player, new TremorSenseCastData(
                (int) Math.round(byLevel(level, 60, 180, 256) * AbilityAPI.getAbilityPower(player))));
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (!(AbilityAPI.getUseData(player) instanceof TremorSenseCastData castData)) {
            return;
        }
        if (elapsedTicks % DARKNESS_REFRESH_INTERVAL_TICKS == 1) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
        }
        float chargeFraction = Math.min(1.0f, elapsedTicks / (float) totalTicks);
        int maxRadius = Math.min(40, (int) Math.round(byLevel(level, 10, 15, 20) * 2 * AbilityAPI.getAbilityPower(player)));
        int targetRadius = MIN_SCAN_RADIUS + Math.round(chargeFraction * (maxRadius - MIN_SCAN_RADIUS));
        for (int radius = Math.max(castData.getLastScannedRadius() + 1, MIN_SCAN_RADIUS);
             radius <= targetRadius && !castData.isFull(); radius++) {
            TremorScanner.scanShell(player.serverLevel(), player.blockPosition(), radius, castData);
            castData.setLastScannedRadius(radius);
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (!(AbilityAPI.getUseData(player) instanceof TremorSenseCastData castData)) {
            return;
        }
        player.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,
                player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.6, 0.6, 0.1);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.6f, 1.8f);
        if (castData.hasAnyFound()) {
            PacketDistributor.sendToPlayer(player, new TremorSenseHighlightPacket(
                    player.blockPosition(), castData.getFoundPositions(),
                    HIGHLIGHT_DURATION_TICKS, level >= getMaxLevel()));
        }
    }
}
