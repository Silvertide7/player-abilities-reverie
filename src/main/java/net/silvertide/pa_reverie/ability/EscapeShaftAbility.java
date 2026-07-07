package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class EscapeShaftAbility extends HarvestAbility {
    private static final int[] COOLDOWN_SECONDS_BY_LEVEL = {7200, 5400, 3600};
    private static final double COMPANION_RADIUS = 3.0;

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
        if (player.level().dimension() == Level.NETHER || player.getY() > 0.0
                || player.serverLevel().canSeeSky(player.blockPosition())) {
            return false;
        }
        return surfaceY(player) - player.getY() >= 2;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.escape_shaft_not_deep");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        if (level >= getMaxLevel()) {
            for (ServerPlayer companion : player.serverLevel().getEntitiesOfClass(ServerPlayer.class,
                    new AABB(player.blockPosition()).inflate(COMPANION_RADIUS))) {
                if (companion != player) {
                    riseToSurface(companion);
                }
            }
        }
        riseToSurface(player);
    }

    private void riseToSurface(ServerPlayer player) {
        int targetY = surfaceY(player) + 1;
        player.serverLevel().sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                player.getX(), player.getY() + 1, player.getZ(), 24, 0.3, 0.5, 0.3, 0.0);
        player.teleportTo(player.serverLevel(), player.getX(), targetY, player.getZ(),
                player.getYRot(), player.getXRot());
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
        player.serverLevel().sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                player.getX(), player.getY(), player.getZ(), 24, 0.4, 0.4, 0.4, 0.0);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private int surfaceY(ServerPlayer player) {
        return player.serverLevel().getHeight(Heightmap.Types.OCEAN_FLOOR, player.getBlockX(), player.getBlockZ());
    }
}
