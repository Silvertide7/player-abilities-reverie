package net.silvertide.pa_reverie.ability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.silvertide.pa_reverie.config.ServerConfigs;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class FeastOfLifeAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1200;
    private static final float MAX_ABSORPTION = 40.0f;
    private static final float[] POTENCY_BY_LEVEL = {1.0f, 1.35f, 1.7f};
    private static final float[] EFFECT_DURATION_MULTIPLIER_BY_LEVEL = {1.2f, 1.3f, 1.4f};
    private static final double FEAST_SHARE_RADIUS = 4.0;
    private static final int FEAST_PARTICLE_COUNT = 12;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 60;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        return findFoodStack(player) != null;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.feast_of_life_no_food");
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        ItemStack food = findFoodStack(player);
        if (food == null) {
            return;
        }
        FoodProperties properties = food.get(DataComponents.FOOD);
        int clampedIndex = Math.clamp(level, 1, getMaxLevel()) - 1;
        float potency = POTENCY_BY_LEVEL[clampedIndex];
        float qualityMultiplier = 1.0f + net.silvertide.pa_reverie.compat.QualityFoodCompat.qualityLevel(food)
                * ServerConfigs.FEAST_OF_LIFE_QUALITY_BONUS_PER_LEVEL.get().floatValue();
        float multiplier = potency * qualityMultiplier;

        float healAmount = (float) ReverieMagicAttributes.scaledByHarvestPower(
                player, properties.nutrition() * ServerConfigs.FEAST_OF_LIFE_HEALTH_PER_NUTRITION.get().floatValue() * multiplier);
        float absorptionAmount = (float) ReverieMagicAttributes.scaledByHarvestPower(
                player, properties.saturation() * ServerConfigs.FEAST_OF_LIFE_ABSORPTION_PER_SATURATION.get().floatValue() * multiplier);

        for (Player target : player.level().getEntitiesOfClass(
                Player.class, player.getBoundingBox().inflate(FEAST_SHARE_RADIUS))) {
            target.heal(healAmount);
            target.setAbsorptionAmount(Math.min(MAX_ABSORPTION, target.getAbsorptionAmount() + absorptionAmount));
            applyFoodEffects(target, properties, clampedIndex);
            spawnFeastParticles(player.serverLevel(), target);
        }
        food.shrink(1);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    private static ItemStack findFoodStack(LivingEntity entity) {
        ItemStack mainHand = entity.getMainHandItem();
        if (isFood(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = entity.getOffhandItem();
        if (isFood(offHand)) {
            return offHand;
        }
        return null;
    }

    private static boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.FOOD);
    }

    private static void applyFoodEffects(LivingEntity entity, FoodProperties properties, int clampedIndex) {
        float durationMultiplier = EFFECT_DURATION_MULTIPLIER_BY_LEVEL[clampedIndex];
        for (FoodProperties.PossibleEffect possibleEffect : properties.effects()) {
            MobEffectInstance effect = possibleEffect.effect();
            entity.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    Math.round(effect.getDuration() * durationMultiplier),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()));
        }
    }

    private static void spawnFeastParticles(ServerLevel level, LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.6;
        double z = entity.getZ();
        level.sendParticles(ParticleTypes.HEART, x, y, z, FEAST_PARTICLE_COUNT, 0.4, 0.4, 0.4, 0.1);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, FEAST_PARTICLE_COUNT, 0.4, 0.4, 0.4, 0.1);
    }
}
