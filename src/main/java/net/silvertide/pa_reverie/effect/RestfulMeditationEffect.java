package net.silvertide.pa_reverie.effect;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.silvertide.pa_reverie.registry.ReverieEffects;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RestfulMeditationEffect extends MobEffect {

    private static final Map<UUID, Vec3> LOCKED_POSITIONS = new ConcurrentHashMap<>();

    private static final double MOVEMENT_THRESHOLD_SQR = 0.5 * 0.5;

    private static final int POSITION_CHECK_INTERVAL_TICKS = 10;

    private static final int[] HEAL_INTERVAL_TICKS_BY_AMPLIFIER = { 40, 30, 20 };
    private static final int[] FOOD_INTERVAL_TICKS_BY_AMPLIFIER = { 60, 45, 30 };

    private static final float HEAL_PER_PULSE = 1.0f;
    private static final int FOOD_PER_PULSE = 1;
    private static final float SATURATION_PER_PULSE = 1.0f;
    private static final int MAX_FOOD_LEVEL = 20;

    private static final float ENDED_SOUND_VOLUME = 0.5f;
    private static final float ENDED_SOUND_PITCH = 1.2f;

    public RestfulMeditationEffect(MobEffectCategory category, int color) {
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
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }
        if (entity.tickCount % POSITION_CHECK_INTERVAL_TICKS == 0 && hasLeftLockedPosition(entity)) {
            removeAfterCurrentTick(entity);
            return;
        }

        int healInterval = intervalForAmplifier(HEAL_INTERVAL_TICKS_BY_AMPLIFIER, amplifier);
        if (entity.tickCount % healInterval == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(HEAL_PER_PULSE);
        }
        if (entity instanceof Player player) {
            int foodInterval = intervalForAmplifier(FOOD_INTERVAL_TICKS_BY_AMPLIFIER, amplifier);
            if (entity.tickCount % foodInterval == 0 && player.getFoodData().getFoodLevel() < MAX_FOOD_LEVEL) {
                player.getFoodData().eat(FOOD_PER_PULSE, SATURATION_PER_PULSE);
            }
        }
    }

    private static boolean hasLeftLockedPosition(LivingEntity entity) {
        Vec3 lockedPosition = LOCKED_POSITIONS.get(entity.getUUID());
        return lockedPosition == null
                || lockedPosition.distanceToSqr(entity.position()) > MOVEMENT_THRESHOLD_SQR;
    }

    private static void removeAfterCurrentTick(LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server != null) {
            server.tell(new TickTask(server.getTickCount(), () -> {
                if (entity.removeEffect(ReverieEffects.RESTFUL_MEDITATION_EFFECT.get())) {
                    onMeditationEnded(entity);
                }
            }));
        }
    }

    private static int intervalForAmplifier(int[] intervalsByAmplifier, int amplifier) {
        return intervalsByAmplifier[Mth.clamp(amplifier, 0, intervalsByAmplifier.length - 1)];
    }

    public static void onMeditationEnded(LivingEntity entity) {
        LOCKED_POSITIONS.remove(entity.getUUID());
        entity.level().playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                ENDED_SOUND_VOLUME,
                ENDED_SOUND_PITCH
        );
    }
}
