package net.silvertide.pa_reverie.effect;

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
    private final Map<LivingEntity, Integer> entityPreviousDurations = new WeakHashMap<>();

    protected VisionEffect(MobEffectCategory category, int displayColor) {
        super(category, displayColor);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            return;
        }
        MobEffectInstance instance = entity.getEffect(this);
        if (instance == null) {
            return;
        }
        int duration = instance.getDuration();
        Integer previousDuration = entityPreviousDurations.put(entity, duration);
        if (previousDuration == null || duration > previousDuration) {
            entityStartDurations.put(entity, duration);
        }
    }

    public static float getIntensity(Player player, MobEffect effectType, float partialTicks) {
        MobEffectInstance instance = player.getEffect(effectType);
        if (instance == null) {
            return 0.0F;
        }
        VisionEffect effect = (VisionEffect) instance.getEffect();
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
