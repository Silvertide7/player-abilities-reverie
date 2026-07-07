package net.silvertide.pa_reverie.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class ShepherdsAuraAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int AURA_INTERVAL_TICKS = 20;
    private static final int MAX_ANIMALS_PER_PULSE = 64;
    private static final double LEAD_DISTANCE = 4.0;
    private static final double LEAD_SPEED = 1.1;
    private static final int BOON_DURATION_TICKS = 40;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 20;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public int getEffectDurationTicks(int level) {
        return byLevel(level, 1200, 1800, 2400);
    }

    @Override
    public void onEffectTick(ServerPlayer player, int level, int remainingTicks) {
        if (remainingTicks % AURA_INTERVAL_TICKS != 0) {
            return;
        }
        double abilityPower = AbilityAPI.getAbilityPower(player);
        int auraRadius = Math.min(36, (int) Math.round(byLevel(level, 10, 15, 20) * abilityPower));
        float healPerPulse = (float) (byLevel(level, 1.0f, 1.0f, 2.0f) * abilityPower);
        int resistanceAmplifier = level >= getMaxLevel() ? 1 : 0;
        List<Animal> flock = player.serverLevel().getEntitiesOfClass(Animal.class,
                new AABB(player.blockPosition()).inflate(auraRadius));
        int tended = 0;
        for (Animal animal : flock) {
            if (tended >= MAX_ANIMALS_PER_PULSE) {
                break;
            }
            if (animal.getHealth() < animal.getMaxHealth()) {
                animal.heal(healPerPulse);
            }
            animal.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BOON_DURATION_TICKS,
                    resistanceAmplifier, true, false));
            if (level >= getMaxLevel()) {
                animal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BOON_DURATION_TICKS, 0, true, false));
            }
            if (animal.distanceTo(player) > LEAD_DISTANCE) {
                animal.getNavigation().moveTo(player, LEAD_SPEED);
            }
            if (animal.getTarget() == player) {
                animal.setTarget(null);
            }
            tended++;
        }
    }
}
