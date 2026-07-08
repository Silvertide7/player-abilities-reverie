package net.silvertide.pa_reverie.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.pa_reverie.network.HunterHighlightPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HunterEffect extends MobEffect {

    private static final int BLOCKS_PER_CHUNK = 16;
    private static final int[] SCAN_RADIUS_BY_AMPLIFIER = {
            BLOCKS_PER_CHUNK * 3 / 2,
            BLOCKS_PER_CHUNK * 2,
            BLOCKS_PER_CHUNK * 5 / 2
    };
    private static final int SCAN_INTERVAL_TICKS = 40;
    private static final int HIGHLIGHT_TTL_TICKS = 50;
    private static final int MAX_HIGHLIGHTED_ENTITIES = 256;
    private static final int SCAN_RADIUS_CAP = 80;

    public HunterEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static int scanRadiusForAmplifier(int amplifier) {
        return SCAN_RADIUS_BY_AMPLIFIER[Math.clamp(amplifier, 0, SCAN_RADIUS_BY_AMPLIFIER.length - 1)];
    }

    private static int scaledScanRadius(ServerPlayer caster, int amplifier) {
        double harvestPower = caster.getAttributeValue(ReverieMagicAttributes.HARVEST_SPELL_POWER);
        return Math.min(SCAN_RADIUS_CAP, (int) Math.round(scanRadiusForAmplifier(amplifier) * harvestPower));
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof ServerPlayer caster) {
            AABB scanBox = caster.getBoundingBox().inflate(scaledScanRadius(caster, amplifier));
            List<Integer> targetIds = caster.serverLevel()
                    .getEntitiesOfClass(LivingEntity.class, scanBox, candidate -> isHighlightable(candidate, caster, amplifier))
                    .stream()
                    .limit(MAX_HIGHLIGHTED_ENTITIES)
                    .map(Entity::getId)
                    .toList();
            if (!targetIds.isEmpty()) {
                PacketDistributor.sendToPlayer(caster, new HunterHighlightPacket(targetIds, HIGHLIGHT_TTL_TICKS));
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % SCAN_INTERVAL_TICKS == 0;
    }

    private static boolean isHighlightable(LivingEntity candidate, ServerPlayer caster, int amplifier) {
        if (candidate == caster || !candidate.isAlive() || candidate instanceof WaterAnimal) {
            return false;
        }
        return switch (amplifier) {
            case 0 -> candidate instanceof Animal;
            case 1 -> candidate instanceof Mob && !(candidate instanceof Enemy);
            default -> candidate instanceof Mob || candidate instanceof Player;
        };
    }
}
