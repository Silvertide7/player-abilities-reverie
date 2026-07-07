package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.silvertide.pa_reverie.support.TransmuteRecipes;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class TransmuteAbility extends HarvestAbility {
    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHARGED;
    }

    @Override
    public int getUseTicks(int level) {
        return 200;
    }

    @Override
    public int getCooldownTicks(int level) {
        return 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        return TransmuteRecipes.findBestMatch(player.getMainHandItem(), level) != null;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.transmute_no_recipe");
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (elapsedTicks % 8 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.WAX_OFF,
                    player.getX(), player.getEyeY() + 0.3, player.getZ(), 6, 0.5, 0.2, 0.5, 0.0);
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        ItemStack held = player.getMainHandItem();
        TransmuteRecipes.TransmuteRecipe recipe = TransmuteRecipes.findBestMatch(held, level);
        if (recipe == null) {
            return;
        }
        int batches = Math.min(recipe.maxConversions(), held.getCount() / recipe.inputCount());
        held.shrink(recipe.inputCount() * batches);
        ItemStack result = TransmuteRecipes.resultFor(recipe, batches);
        if (!player.getInventory().add(result)) {
            Block.popResource(player.serverLevel(), player.blockPosition(), result);
        }
        player.serverLevel().sendParticles(ParticleTypes.WAX_ON,
                player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.4, 0.6, 0.4, 0.1);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (recipe.cooldownSeconds() > 0 && !player.getAbilities().instabuild) {
            AbilityAPI.setCooldown(player, this, recipe.cooldownSeconds() * 20);
        }
    }
}
