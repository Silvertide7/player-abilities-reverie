package net.silvertide.pa_reverie.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.silvertide.player_abilities.api.AbilityTrigger;
import net.silvertide.player_abilities.api.PlayerTriggers;
import net.silvertide.player_abilities.api.TriggeredAbility;

public final class GuardianAngelAbility extends TriggeredAbility<PlayerTriggers.DamageTaken> {
    private static final int COOLDOWN_SECONDS = 7200;
    private static final int TICKS_PER_SECOND = 20;

    @Override
    public AbilityTrigger<PlayerTriggers.DamageTaken> getTrigger() {
        return PlayerTriggers.LETHAL_DAMAGE;
    }

    @Override
    public int getCooldownTicks(int level) {
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public void onTrigger(ServerPlayer player, int level) {
        player.setHealth(player.getMaxHealth() / 2.0f);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
