package net.silvertide.pa_reverie.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShepherdsAuraEffect extends MobEffect {

    private static final int[] AURA_RADIUS_BY_AMPLIFIER = { 10, 15, 20 };
    private static final int AURA_RADIUS_CAP = 36;
    private static final float[] HEAL_AMOUNT_BY_AMPLIFIER = { 1.0f, 1.0f, 2.0f };

    private static final int AURA_TICK_INTERVAL_TICKS = 20;
    private static final int BOON_DURATION_TICKS = 40;
    private static final int MAX_FLOCK_SIZE = 64;

    private static final int LEVEL_3_AMPLIFIER = 2;
    private static final int RESISTANCE_I = 0;
    private static final int RESISTANCE_II = 1;
    private static final int SPEED_I = 0;

    private static final double FOLLOW_START_DISTANCE = 4.0;
    private static final double FOLLOW_SPEED = 1.1;

    public ShepherdsAuraEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static int auraRadiusForAmplifier(int amplifier) {
        return AURA_RADIUS_BY_AMPLIFIER[Math.clamp(amplifier, 0, AURA_RADIUS_BY_AMPLIFIER.length - 1)];
    }

    private static int scaledAuraRadius(ServerPlayer shepherd, int amplifier) {
        double harvestPower = shepherd.getAttributeValue(ReverieMagicAttributes.HARVEST_SPELL_POWER);
        return Math.min(AURA_RADIUS_CAP, (int) Math.round(auraRadiusForAmplifier(amplifier) * harvestPower));
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof ServerPlayer shepherd) {
            tendFlock(shepherd, amplifier);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % AURA_TICK_INTERVAL_TICKS == 0;
    }

    private void tendFlock(ServerPlayer shepherd, int amplifier) {
        AABB auraBox = shepherd.getBoundingBox().inflate(scaledAuraRadius(shepherd, amplifier));
        List<Animal> flock = shepherd.serverLevel().getEntitiesOfClass(Animal.class, auraBox, Animal::isAlive);
        float healAmount = (float) ReverieMagicAttributes.scaledByHarvestPower(shepherd, HEAL_AMOUNT_BY_AMPLIFIER[Math.clamp(amplifier, 0, HEAL_AMOUNT_BY_AMPLIFIER.length - 1)]);
        int tended = 0;
        for (Animal animal : flock) {
            if (tended >= MAX_FLOCK_SIZE) {
                break;
            }
            healAnimal(animal, healAmount);
            applyBoons(animal, amplifier);
            leadToShepherd(animal, shepherd);
            calmTowardShepherd(animal, shepherd);
            tended++;
        }
    }

    private static void healAnimal(Animal animal, float healAmount) {
        if (animal.getHealth() < animal.getMaxHealth()) {
            animal.heal(healAmount);
        }
    }

    private static void applyBoons(Animal animal, int amplifier) {
        int resistanceLevel = amplifier >= LEVEL_3_AMPLIFIER ? RESISTANCE_II : RESISTANCE_I;
        animal.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BOON_DURATION_TICKS, resistanceLevel, true, true, false));
        if (amplifier >= LEVEL_3_AMPLIFIER) {
            animal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BOON_DURATION_TICKS, SPEED_I, true, true, false));
        }
    }

    private static void leadToShepherd(Animal animal, ServerPlayer shepherd) {
        if (animal instanceof TamableAnimal tamable && tamable.isTame()) {
            return;
        }
        if (animal.distanceToSqr(shepherd) <= FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE) {
            return;
        }
        animal.getNavigation().moveTo(shepherd.getX(), shepherd.getY(), shepherd.getZ(), FOLLOW_SPEED);
    }

    private static void calmTowardShepherd(Animal animal, ServerPlayer shepherd) {
        if (animal.getTarget() == shepherd) {
            animal.setTarget(null);
        }
    }
}
