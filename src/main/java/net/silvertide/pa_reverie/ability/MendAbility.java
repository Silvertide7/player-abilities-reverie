package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class MendAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHANNELED;
    }

    @Override
    public int getUseTicks(int level) {
        return 100;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public boolean canUse(ServerPlayer player, int level) {
        return findRepairableStack(player) != null;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.mend_nothing_to_mend");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        ItemStack repairable = findRepairableStack(player);
        if (repairable == null) {
            return;
        }
        float repairPercentPerTick = (float) (byLevel(level, 0.004f, 0.00533f, 0.00667f)
                * AbilityAPI.getAbilityPower(player));
        int repairAmount = Math.max(1, Math.round(repairable.getMaxDamage() * repairPercentPerTick));
        repairable.setDamageValue(Math.max(0, repairable.getDamageValue() - repairAmount));
        if (elapsedTicks % 4 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + player.getBbHeight() * 0.7, player.getZ(), 8, 0.4, 0.4, 0.4, 0.3);
        }
        if (elapsedTicks % 12 == 0) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.35f, 1.5f);
        }
    }

    private static ItemStack findRepairableStack(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.isDamageableItem() && held.isDamaged()) {
                return held;
            }
        }
        return null;
    }
}
