package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class MendAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int CAST_TIME_TICKS = 100;
    private static final int BASE_SPELL_POWER = 8;
    private static final int SPELL_POWER_PER_LEVEL = 6;
    private static final float MIN_LEVEL_SPELL_POWER = (float) BASE_SPELL_POWER;
    private static final float MAX_LEVEL_SPELL_POWER = (float) (BASE_SPELL_POWER + 2 * SPELL_POWER_PER_LEVEL);
    private static final float REPAIR_PERCENT_AT_MIN_LEVEL = 0.40f;
    private static final float REPAIR_PERCENT_AT_MAX_LEVEL = 0.80f;
    private static final float REPAIR_PERCENT_PER_SPELL_POWER =
            (REPAIR_PERCENT_AT_MAX_LEVEL - REPAIR_PERCENT_AT_MIN_LEVEL) / (MAX_LEVEL_SPELL_POWER - MIN_LEVEL_SPELL_POWER);
    private static final int PARTICLE_TICK_INTERVAL = 4;
    private static final int ANVIL_SOUND_TICK_INTERVAL = 12;
    private static final float ANVIL_TICK_VOLUME = 0.35f;
    private static final float ANVIL_TICK_PITCH = 1.5f;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHANNELED;
    }

    @Override
    public int getUseTicks(int level) {
        return CAST_TIME_TICKS;
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
        ItemStack target = findRepairableStack(player);
        if (target == null) {
            return;
        }
        float power = spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level);
        int repairAmount = Math.max(1, Math.round(target.getMaxDamage() * repairPercentPerTick(power)));
        target.setDamageValue(Math.max(0, target.getDamageValue() - repairAmount));
        if (player.tickCount % PARTICLE_TICK_INTERVAL == 0) {
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + player.getBbHeight() * 0.7, player.getZ(), 8, 0.4, 0.4, 0.4, 0.3);
        }
        if (player.tickCount % ANVIL_SOUND_TICK_INTERVAL == 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, ANVIL_TICK_VOLUME, ANVIL_TICK_PITCH);
        }
    }

    @Override
    public void onUseComplete(ServerPlayer player, int level, boolean cancelled) {
        if (cancelled && !player.isCreative()
                && net.silvertide.player_abilities.api.AbilityAPI.getActiveUse(player)
                        .map(use -> use.getElapsedTicks() > 0).orElse(false)) {
            net.silvertide.player_abilities.api.AbilityAPI.setCooldown(player, this, getCooldownTicks(level));
        }
    }

    private static float repairPercentPerTick(float power) {
        float percentPerCast = REPAIR_PERCENT_AT_MIN_LEVEL
                + Math.max(0f, power - MIN_LEVEL_SPELL_POWER) * REPAIR_PERCENT_PER_SPELL_POWER;
        return percentPerCast / CAST_TIME_TICKS;
    }

    private static ItemStack findRepairableStack(LivingEntity entity) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = entity.getItemInHand(hand);
            if (held.isDamageableItem() && held.isDamaged()) {
                return held;
            }
        }
        return null;
    }
}
