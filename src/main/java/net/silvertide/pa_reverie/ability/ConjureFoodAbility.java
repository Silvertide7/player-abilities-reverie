package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.silvertide.pa_reverie.food.ConjuredFoods;
import net.silvertide.pa_reverie.item.EphemeralFoodItem;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class ConjureFoodAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int CONJURE_PARTICLE_COUNT = 15;

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
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        EphemeralFoodItem conjuredItem = ConjuredFoods.BY_TIER
                .get(Math.clamp(level, 1, getMaxLevel()) - 1).get();
        ItemStack conjured = new ItemStack(conjuredItem);
        EphemeralFoodItem.setExpiration(conjured, player.level(), conjuredItem.defaultLifetimeTicks());
        if (!player.addItem(conjured)) {
            player.drop(conjured, false);
        }
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                CONJURE_PARTICLE_COUNT, 0.4, 0.4, 0.4, 0.2);
    }
}
