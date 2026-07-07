package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.silvertide.pa_reverie.item.EphemeralFoodItem;
import net.silvertide.pa_reverie.registry.ReverieItems;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class ConjureFoodAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
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
        EphemeralFoodItem conjuredFood = switch (level) {
            case 1 -> ReverieItems.EPHEMERAL_BISCUIT.get();
            case 2 -> ReverieItems.EPHEMERAL_NECTAR.get();
            default -> ReverieItems.EPHEMERAL_FEAST.get();
        };
        ItemStack conjuredStack = new ItemStack(conjuredFood);
        EphemeralFoodItem.setExpiration(conjuredStack, player.level(), conjuredFood.defaultLifetimeTicks());
        if (!player.getInventory().add(conjuredStack)) {
            player.level().addFreshEntity(new ItemEntity(player.level(),
                    player.getX(), player.getY(), player.getZ(), conjuredStack));
        }
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(), 15, 0.4, 0.4, 0.4, 0.2);
    }
}
