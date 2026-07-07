package net.silvertide.pa_reverie.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.silvertide.player_abilities.api.AbilityUseType;
import net.silvertide.player_abilities.api.EffectGrant;

import java.util.List;

public final class DeepsightAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 300;
    private static final int DEPTH_BELOW_SURFACE_REQUIRED = 64;

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
    public boolean canUse(ServerPlayer player, int level) {
        if (player.level().dimension() == Level.NETHER) {
            return true;
        }
        int surfaceY = player.serverLevel().getHeight(Heightmap.Types.WORLD_SURFACE, player.getBlockX(), player.getBlockZ());
        return player.getY() < surfaceY - DEPTH_BELOW_SURFACE_REQUIRED;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.deepsight_too_shallow");
    }

    @Override
    public List<EffectGrant> getEffectGrants(int level) {
        return List.of(new EffectGrant(MobEffects.NIGHT_VISION, byLevel(level, 3600, 7200, 18000), 0));
    }
}
