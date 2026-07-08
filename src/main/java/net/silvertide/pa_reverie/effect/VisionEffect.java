package net.silvertide.pa_reverie.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class VisionEffect extends MobEffect {

    protected static final int INTENSITY_FADE_TICKS = 20;
    protected static final float INTENSITY_PER_FADE_TICK = 1.0f / INTENSITY_FADE_TICKS;

    private final Map<LivingEntity, Integer> entityStartDurations = new WeakHashMap<>();

    protected VisionEffect(MobEffectCategory category, int displayColor) {
        super(category, displayColor);
    }

    protected abstract Holder<MobEffect> getEffectHolder();

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            return true;
        }
        MobEffectInstance instance = entity.getEffect(getEffectHolder());
        if (instance != null) {
            int duration = instance.getDuration();
            Integer recorded = entityStartDurations.get(entity);
            if (recorded == null || duration > recorded) {
                entityStartDurations.put(entity, duration);
            }
        }
        return true;
    }

    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            return;
        }
        MobEffectInstance instance = entity.getEffect(getEffectHolder());
        if (instance != null) {
            entityStartDurations.put(entity, instance.getDuration());
        }
    }

    public static float getIntensity(Player player, Holder<MobEffect> effectHolder, float partialTicks) {
        MobEffectInstance instance = player.getEffect(effectHolder);
        if (instance == null) {
            return 0.0F;
        }
        if (instance.isInfiniteDuration()) {
            return 1.0F;
        }
        VisionEffect effect = (VisionEffect) instance.getEffect().value();
        int duration = instance.getDuration();
        int maxDuration = effect.entityStartDurations.getOrDefault(player, duration);
        if (duration > maxDuration) {
            maxDuration = duration;
            effect.entityStartDurations.put(player, maxDuration);
        }
        float activeTime = maxDuration - duration + partialTicks;
        float cappedByRemaining = Math.min(activeTime, duration + partialTicks);
        return Math.min(INTENSITY_FADE_TICKS, cappedByRemaining) * INTENSITY_PER_FADE_TICK;
    }
}
