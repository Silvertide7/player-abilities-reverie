package net.silvertide.pa_reverie.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.silvertide.pa_reverie.support.TreeScanner;
import net.silvertide.pa_reverie.support.TreeYields;
import net.silvertide.pa_reverie.support.WoodsongCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class WoodsongAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 2400;
    private static final int MIN_SCAN_RADIUS = 4;
    private static final int RING_PARTICLE_POINTS = 24;

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
        AbilityAPI.setUseData(player, new WoodsongCastData());
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (!(AbilityAPI.getUseData(player) instanceof WoodsongCastData castData)) {
            return;
        }
        float chargeFraction = Math.min(1.0f, elapsedTicks / (float) totalTicks);
        int maxRadius = Math.min(40, (int) Math.round(byLevel(level, 8, 14, 20) * AbilityAPI.getAbilityPower(player)));
        int targetRadius = MIN_SCAN_RADIUS + Math.round(chargeFraction * (maxRadius - MIN_SCAN_RADIUS));
        for (int radius = Math.max(castData.getLastScannedRadius() + 1, MIN_SCAN_RADIUS); radius <= targetRadius; radius++) {
            TreeScanner.scanShell(player.serverLevel(), player.blockPosition(), radius, castData);
            castData.setLastScannedRadius(radius);
        }
        if (elapsedTicks % 3 == 0) {
            spawnScanRing(player, targetRadius);
        }
    }

    @Override
    public void onUseComplete(ServerPlayer player, int level, boolean cancelled) {
        if (!(AbilityAPI.getUseData(player) instanceof WoodsongCastData castData)
                || !castData.hasFoundAnyTrees()) {
            return;
        }
        castData.forEachFoundTree((log, treeCount) -> awardTreeYields(player, log, treeCount));
        player.serverLevel().sendParticles(ParticleTypes.COMPOSTER,
                player.getX(), player.getY() + 1.0, player.getZ(), 32, 0.5, 0.5, 0.5, 0.1);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (cancelled) {
            AbilityAPI.setCooldown(player, this, getCooldownTicks(level));
        }
    }

    private void awardTreeYields(ServerPlayer player, Block log, int treeCount) {
        TreeYields.TreeYield yield = TreeYields.lookup(log);
        int logCount = 0;
        int leafCount = 0;
        for (int tree = 0; tree < treeCount; tree++) {
            logCount += Mth.randomBetweenInclusive(player.getRandom(), 2, 6);
            leafCount += Mth.randomBetweenInclusive(player.getRandom(), 1, 3);
        }
        giveOrDrop(player, new ItemStack(log.asItem(), logCount));
        if (yield != null) {
            giveOrDrop(player, new ItemStack(yield.leaves().asItem(), leafCount));
            giveOrDrop(player, new ItemStack(net.minecraft.world.item.Items.STICK,
                    player.getRandom().nextInt(3 * treeCount + 1)));
            giveOrDrop(player, new ItemStack(yield.sapling(),
                    player.getRandom().nextInt(2 * treeCount + 1)));
        }
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            Block.popResource(player.serverLevel(), player.blockPosition(), stack);
        }
    }

    private static void spawnScanRing(ServerPlayer player, int radius) {
        for (int point = 0; point < RING_PARTICLE_POINTS; point++) {
            double angle = Math.PI * 2 * point / RING_PARTICLE_POINTS;
            player.serverLevel().sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    player.getX() + Math.cos(angle) * radius,
                    player.getY() + 1.0,
                    player.getZ() + Math.sin(angle) * radius, 2, 0.1, 0.3, 0.1, 0.0);
        }
    }
}
