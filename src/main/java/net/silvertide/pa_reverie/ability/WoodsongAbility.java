package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.silvertide.pa_reverie.support.ReverieMagicAttributes;
import net.silvertide.pa_reverie.support.TreeScanner;
import net.silvertide.pa_reverie.support.TreeYields;
import net.silvertide.pa_reverie.support.WoodsongCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.concurrent.ThreadLocalRandom;

public final class WoodsongAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 2400;
    private static final int BASE_SPELL_POWER = 8;
    private static final int SPELL_POWER_PER_LEVEL = 6;
    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS_CAP = 40;
    private static final int MIN_LOGS_PER_TREE = 2;
    private static final int MAX_LOGS_PER_TREE = 6;
    private static final int MIN_LEAVES_PER_TREE = 1;
    private static final int MAX_LEAVES_PER_TREE = 3;
    private static final int MAX_BONUS_STICKS_PER_TREE = 3;
    private static final int MAX_BONUS_SAPLINGS_PER_TREE = 2;
    private static final int PARTICLE_RING_POINTS = 24;
    private static final int PARTICLES_PER_RING_POINT = 2;
    private static final int PARTICLE_RING_TICK_INTERVAL = 3;

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
        return COOLDOWN_SECONDS * TICKS_PER_SECOND;
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        ServerLevel serverLevel = player.serverLevel();
        WoodsongCastData castData;
        if (AbilityAPI.getUseData(player) instanceof WoodsongCastData existing) {
            castData = existing;
        } else {
            castData = new WoodsongCastData();
            AbilityAPI.setUseData(player, castData);
        }
        float chargeFraction = Math.min(1.0f, elapsedTicks / (float) totalTicks);
        int maxRadius = Math.max(MIN_RADIUS,
                Math.min(MAX_RADIUS_CAP, Math.round(spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level))));
        int targetRadius = MIN_RADIUS + Math.round(chargeFraction * (maxRadius - MIN_RADIUS));
        int lastScanned = castData.getLastScannedRadius();
        if (targetRadius > lastScanned) {
            BlockPos center = player.blockPosition();
            int startRadius = Math.max(lastScanned + 1, MIN_RADIUS);
            for (int radius = startRadius; radius <= targetRadius; radius++) {
                TreeScanner.scanShell(serverLevel, center, radius, castData);
            }
            castData.setLastScannedRadius(targetRadius);
        }
        if (player.tickCount % PARTICLE_RING_TICK_INTERVAL == 0) {
            spawnScanRingParticles(serverLevel, player, Math.max(targetRadius, MIN_RADIUS));
        }
    }

    @Override
    public void onUseComplete(ServerPlayer player, int level, boolean cancelled) {
        if (AbilityAPI.getUseData(player) instanceof WoodsongCastData castData
                && castData.hasFoundAnyTrees()) {
            awardYield(player.level(), player, castData);
            if (cancelled) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (!player.isCreative()) {
                    AbilityAPI.setCooldown(player, this, getCooldownTicks(level));
                }
            }
        }
    }

    private void awardYield(Level level, LivingEntity entity, WoodsongCastData castData) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BlockPos dropPos = entity.blockPosition();
        castData.forEachFoundTree((logBlock, treeCount) -> {
            TreeYields.TreeYield yield = TreeYields.lookup(logBlock);
            if (yield == null) {
                return;
            }
            int totalLogs = ReverieMagicAttributes.scaledByHarvestPower(entity,
                    rollPerTreeInclusive(random, MIN_LOGS_PER_TREE, MAX_LOGS_PER_TREE, treeCount));
            int totalLeaves = ReverieMagicAttributes.scaledByHarvestPower(entity,
                    rollPerTreeInclusive(random, MIN_LEAVES_PER_TREE, MAX_LEAVES_PER_TREE, treeCount));
            int totalSticks = ReverieMagicAttributes.scaledByHarvestPower(entity,
                    rollBonus(random, MAX_BONUS_STICKS_PER_TREE, treeCount));
            int totalSaplings = ReverieMagicAttributes.scaledByHarvestPower(entity,
                    rollBonus(random, MAX_BONUS_SAPLINGS_PER_TREE, treeCount));
            dropToPlayer(level, dropPos, entity, new ItemStack(logBlock, totalLogs));
            dropToPlayer(level, dropPos, entity, new ItemStack(yield.leaves(), totalLeaves));
            if (totalSticks > 0) {
                dropToPlayer(level, dropPos, entity, new ItemStack(Items.STICK, totalSticks));
            }
            if (totalSaplings > 0) {
                dropToPlayer(level, dropPos, entity, new ItemStack(yield.sapling(), totalSaplings));
            }
        });
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    dropPos.getX() + 0.5, dropPos.getY() + 1.0, dropPos.getZ() + 0.5,
                    32, 0.5, 0.5, 0.5, 0.0);
        }
    }

    private static int rollBonus(ThreadLocalRandom random, int maxPerTree, int treeCount) {
        int max = maxPerTree * treeCount;
        return max <= 0 ? 0 : random.nextInt(max + 1);
    }

    private static int rollPerTreeInclusive(ThreadLocalRandom random, int minPerTree, int maxPerTree, int treeCount) {
        int total = 0;
        for (int i = 0; i < treeCount; i++) {
            total += random.nextInt(minPerTree, maxPerTree + 1);
        }
        return total;
    }

    private static void dropToPlayer(Level level, BlockPos pos, LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (entity instanceof Player player) {
            if (!player.getInventory().add(stack)) {
                Block.popResource(level, pos, stack);
            }
        } else {
            Block.popResource(level, pos, stack);
        }
    }

    private static void spawnScanRingParticles(ServerLevel level, LivingEntity entity, int radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double cx = entity.getX();
        double cy = entity.getY() + 0.1;
        double cz = entity.getZ();
        double angleJitter = (Math.PI * 2.0) / (PARTICLE_RING_POINTS * 2);
        for (int i = 0; i < PARTICLE_RING_POINTS; i++) {
            double theta = (Math.PI * 2.0 * i) / PARTICLE_RING_POINTS
                    + (random.nextDouble() * 2.0 - 1.0) * angleJitter;
            double r = radius + (random.nextDouble() * 0.8 - 0.4);
            double x = cx + Math.cos(theta) * r;
            double z = cz + Math.sin(theta) * r;
            double y = cy + 0.2 + random.nextDouble() * 0.8;
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    x, y, z, PARTICLES_PER_RING_POINT, 0.15, 0.1, 0.15, 0.02);
        }
    }
}
