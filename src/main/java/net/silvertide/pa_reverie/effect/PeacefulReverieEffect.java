package net.silvertide.pa_reverie.effect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.silvertide.pa_reverie.registry.ReverieEffects;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PeacefulReverieEffect extends MobEffect {

    private static final Map<UUID, Vec3> LOCKED_POSITIONS = new ConcurrentHashMap<>();

    public static final double MOVEMENT_THRESHOLD_SQR = 0.5 * 0.5;

    private static final int POSITION_CHECK_INTERVAL_TICKS = 10;

    private static final float BROKEN_SOUND_VOLUME = 0.5f;
    private static final float BROKEN_SOUND_PITCH = 1.5f;

    public PeacefulReverieEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static void lockPositionFor(LivingEntity entity) {
        LOCKED_POSITIONS.put(entity.getUUID(), entity.position());
    }

    public static void cleanupOnLogout(UUID playerId) {
        LOCKED_POSITIONS.remove(playerId);
    }

    public static void clearLockFor(LivingEntity entity) {
        LOCKED_POSITIONS.remove(entity.getUUID());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return true;
        }
        if (entity.tickCount % POSITION_CHECK_INTERVAL_TICKS == 0) {
            Vec3 lockedPosition = LOCKED_POSITIONS.get(entity.getUUID());
            if (lockedPosition == null) {
                return false;
            }
            if (lockedPosition.distanceToSqr(entity.position()) > MOVEMENT_THRESHOLD_SQR) {
                return false;
            }
        }

        return true;
    }

    public static void onReverieEnded(LivingEntity entity) {
        LOCKED_POSITIONS.remove(entity.getUUID());
        if (entity.hasEffect(MobEffects.INVISIBILITY)) {
            entity.removeEffect(MobEffects.INVISIBILITY);
        }
        entity.level().playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK,
                SoundSource.PLAYERS,
                BROKEN_SOUND_VOLUME,
                BROKEN_SOUND_PITCH
        );
    }

    public static boolean isActiveOn(LivingEntity entity) {
        return entity.hasEffect(ReverieEffects.PEACEFUL_REVERIE_EFFECT);
    }

    public static int amplifierFor(LivingEntity entity) {
        var instance = entity.getEffect(ReverieEffects.PEACEFUL_REVERIE_EFFECT);
        return instance == null ? 0 : instance.getAmplifier();
    }
}
