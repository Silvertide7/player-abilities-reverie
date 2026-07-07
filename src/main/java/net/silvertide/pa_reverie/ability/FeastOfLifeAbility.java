package net.silvertide.pa_reverie.ability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class FeastOfLifeAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1200;
    private static final double SHARE_RADIUS = 4.0;
    private static final float HEALTH_PER_NUTRITION = 1.0f;
    private static final float ABSORPTION_PER_SATURATION = 3.0f;
    private static final float MAX_TOTAL_ABSORPTION = 40.0f;

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
        return findHeldFood(player) != null;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.feast_of_life_no_food");
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        ItemStack food = findHeldFood(player);
        if (food == null) {
            return;
        }
        FoodProperties foodProperties = food.get(DataComponents.FOOD);
        float potency = (float) (byLevel(level, 1.0f, 1.35f, 1.7f)
                * AbilityAPI.getAbilityPower(player));
        float healAmount = foodProperties.nutrition() * HEALTH_PER_NUTRITION * potency;
        float absorptionAmount = foodProperties.saturation() * ABSORPTION_PER_SATURATION * potency;
        float effectDurationMultiplier = byLevel(level, 1.2f, 1.3f, 1.4f);
        for (Player nearby : player.serverLevel().getEntitiesOfClass(Player.class,
                new AABB(player.blockPosition()).inflate(SHARE_RADIUS))) {
            nearby.heal(healAmount);
            nearby.setAbsorptionAmount(Math.min(MAX_TOTAL_ABSORPTION, nearby.getAbsorptionAmount() + absorptionAmount));
            for (FoodProperties.PossibleEffect possibleEffect : foodProperties.effects()) {
                MobEffectInstance effect = possibleEffect.effect();
                nearby.addEffect(new MobEffectInstance(effect.getEffect(),
                        Math.round(effect.getDuration() * effectDurationMultiplier), effect.getAmplifier()));
            }
            player.serverLevel().sendParticles(ParticleTypes.HEART,
                    nearby.getX(), nearby.getY() + nearby.getBbHeight() * 0.6, nearby.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    nearby.getX(), nearby.getY() + nearby.getBbHeight() * 0.6, nearby.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
        }
        food.shrink(1);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    private static ItemStack findHeldFood(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.has(DataComponents.FOOD)) {
                return held;
            }
        }
        return null;
    }
}
