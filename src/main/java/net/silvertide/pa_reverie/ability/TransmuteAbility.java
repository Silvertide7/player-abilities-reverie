package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.silvertide.pa_reverie.transmute.TransmuteRecipe;
import net.silvertide.pa_reverie.transmute.TransmuteRecipes;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class TransmuteAbility extends HarvestAbility {
    private static final int CHANNEL_PARTICLE_TICK_INTERVAL = 8;
    private static final int CHANNEL_PARTICLE_COUNT = 6;
    private static final int COMPLETE_PARTICLE_COUNT = 24;

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
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            AbilityAPI.setUseData(player, "message.pa_reverie.transmute_empty_hand");
            return false;
        }
        TransmuteRecipe recipe = TransmuteRecipes.findBest(player.level(), held, level);
        if (recipe == null) {
            AbilityAPI.setUseData(player, "message.pa_reverie.transmute_no_recipe");
            return false;
        }
        if (held.getCount() < recipe.inputCount()) {
            AbilityAPI.setUseData(player,
                    Component.translatable("message.pa_reverie.transmute_insufficient", recipe.inputCount()));
            return false;
        }
        return true;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        Object failure = AbilityAPI.getUseData(player);
        if (failure instanceof Component failureMessage) {
            return failureMessage;
        }
        return failure instanceof String failureKey
                ? Component.translatable(failureKey)
                : Component.translatable("message.pa_reverie.transmute_no_recipe");
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (player.tickCount % CHANNEL_PARTICLE_TICK_INTERVAL == 0) {
            player.serverLevel().sendParticles(ParticleTypes.WAX_OFF,
                    player.getX(), player.getY() + player.getBbHeight() + 0.3, player.getZ(),
                    CHANNEL_PARTICLE_COUNT, 0.5, 0.2, 0.5, 0.0);
        }
    }

    @Override
    public void onUseReleased(ServerPlayer player, int level) {
        ServerLevel serverLevel = player.serverLevel();
        ItemStack held = player.getMainHandItem();
        TransmuteRecipe recipe = TransmuteRecipes.findBest(serverLevel, held, level);
        if (recipe == null) {
            return;
        }
        int batches = Math.min(recipe.maxConversions(), held.getCount() / recipe.inputCount());
        if (batches <= 0) {
            return;
        }
        held.shrink(recipe.inputCount() * batches);
        giveResult(player, recipe, batches);
        serverLevel.sendParticles(ParticleTypes.WAX_ON,
                player.getX(), player.getY() + 1.0, player.getZ(),
                COMPLETE_PARTICLE_COUNT, 0.4, 0.6, 0.4, 0.1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        applyRecipeCooldown(player, recipe);
    }

    private static void giveResult(ServerPlayer player, TransmuteRecipe recipe, int batches) {
        int remaining = recipe.result().getCount() * batches;
        int maxStackSize = recipe.result().getMaxStackSize();
        while (remaining > 0) {
            int give = Math.min(remaining, maxStackSize);
            ItemStack stack = recipe.result().copyWithCount(give);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= give;
        }
    }

    private void applyRecipeCooldown(ServerPlayer player, TransmuteRecipe recipe) {
        if (recipe.cooldown() <= 0 || player.isCreative()) {
            return;
        }
        AbilityAPI.setCooldown(player, this, recipe.cooldown() * TICKS_PER_SECOND);
    }
}
