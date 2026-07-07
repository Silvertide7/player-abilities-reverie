package net.silvertide.pa_reverie.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class HuntersMarkAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 300;
    private static final int SCAN_INTERVAL_TICKS = 40;
    private static final int GLOW_DURATION_TICKS = 50;
    private static final int MAX_MARKED_ENTITIES = 256;

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
    public int getEffectDurationTicks(int level) {
        return byLevel(level, 600, 900, 1200);
    }

    @Override
    public void onEffectTick(ServerPlayer player, int level, int remainingTicks) {
        if (remainingTicks % SCAN_INTERVAL_TICKS != 0) {
            return;
        }
        int scanRadius = Math.min(80, (int) Math.round(byLevel(level, 24, 32, 40) * AbilityAPI.getAbilityPower(player)));
        List<LivingEntity> nearby = player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                new AABB(player.blockPosition()).inflate(scanRadius));
        int marked = 0;
        for (LivingEntity entity : nearby) {
            if (marked >= MAX_MARKED_ENTITIES) {
                break;
            }
            if (entity == player || !entity.isAlive() || entity instanceof WaterAnimal || !isRevealedAtLevel(entity, level)) {
                continue;
            }
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false));
            marked++;
        }
    }

    private static boolean isRevealedAtLevel(LivingEntity entity, int level) {
        return switch (level) {
            case 1 -> entity instanceof Animal;
            case 2 -> entity instanceof Mob && !(entity instanceof Enemy);
            default -> entity instanceof Mob || entity instanceof Player;
        };
    }
}
