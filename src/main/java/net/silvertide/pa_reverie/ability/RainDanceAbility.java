package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.silvertide.pa_reverie.support.RainDanceCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

public final class RainDanceAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int RAIN_RAMP_TICKS = 100;
    private static final int FARMLAND_PER_TICK = 16;
    private static final int RAIN_SOUND_INTERVAL_TICKS = 35;
    private static final int FULL_MOISTURE = 7;

    @Override
    public AbilityUseType getUseType() {
        return AbilityUseType.CHANNELED;
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
    public boolean canUse(ServerPlayer player, int level) {
        RainDanceCastData scan = scanField(player, level);
        return scan.hasWork();
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.rain_dance_no_field");
    }

    @Override
    public void onUseStart(ServerPlayer player, int level) {
        AbilityAPI.setUseData(player, scanField(player, level));
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        if (!(AbilityAPI.getUseData(player) instanceof RainDanceCastData castData)) {
            return;
        }
        ServerLevel serverLevel = player.serverLevel();
        float rainIntensity = Math.min(1.0f, elapsedTicks / (float) RAIN_RAMP_TICKS);
        for (int irrigated = 0; irrigated < FARMLAND_PER_TICK; irrigated++) {
            BlockPos dry = castData.pollDryFarmland();
            if (dry == null) {
                break;
            }
            BlockState state = serverLevel.getBlockState(dry);
            if (state.getBlock() instanceof FarmBlock) {
                serverLevel.setBlock(dry, state.setValue(FarmBlock.MOISTURE, FULL_MOISTURE), 2);
            }
        }
        int cropsPerTick = Math.max(1, (int) Math.round(byLevel(level, 1, 4, 9) * AbilityAPI.getAbilityPower(player)));
        for (int grown = 0; grown < cropsPerTick; grown++) {
            BlockPos crop = castData.takeNextCropTurn(player.getRandom());
            if (crop == null) {
                break;
            }
            BlockState state = serverLevel.getBlockState(crop);
            BlockState grownState = grownByOneStage(state);
            if (grownState != null) {
                serverLevel.setBlock(crop, grownState, 2);
                if (grownByOneStage(grownState) != null) {
                    castData.keepForNextRound(crop);
                }
            }
        }
        spawnRain(player, castData, rainIntensity);
        if (elapsedTicks % RAIN_SOUND_INTERVAL_TICKS == 0) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.6f * rainIntensity, 1.0f);
        }
        if (!castData.hasWork()) {
            AbilityAPI.finishUse(player);
        }
    }

    private void spawnRain(ServerPlayer player, RainDanceCastData castData, float rainIntensity) {
        if (castData.getRainTargets().isEmpty()) {
            return;
        }
        BlockPos target = castData.getRainTargets()
                .get(player.getRandom().nextInt(castData.getRainTargets().size()));
        int drops = Math.round(12 * rainIntensity);
        double cloudY = player.getY() + player.getBbHeight() + 3.0;
        for (int drop = 0; drop < drops; drop++) {
            double x = target.getX() + 0.5 + (player.getRandom().nextDouble() - 0.5) * 0.5;
            double z = target.getZ() + 0.5 + (player.getRandom().nextDouble() - 0.5) * 0.5;
            double y = Mth.lerp(player.getRandom().nextDouble(), target.getY() + 1.0, cloudY);
            player.serverLevel().sendParticles(ParticleTypes.FALLING_WATER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static BlockState grownByOneStage(BlockState state) {
        for (var property : state.getProperties()) {
            if (property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty ageProperty
                    && ageProperty.getName().equals("age")) {
                int age = state.getValue(ageProperty);
                int maxAge = ageProperty.getPossibleValues().stream().max(Integer::compare).orElse(age);
                return age < maxAge ? state.setValue(ageProperty, age + 1) : null;
            }
        }
        return null;
    }

    private RainDanceCastData scanField(ServerPlayer player, int level) {
        int radius = Mth.clamp((int) Math.round(byLevel(level, 4, 8, 12) * AbilityAPI.getAbilityPower(player)), 4, 24);
        RainDanceCastData castData = new RainDanceCastData();
        BlockPos feet = player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                for (int dy = -2; dy <= 0; dy++) {
                    BlockPos scanned = feet.offset(dx, dy, dz);
                    BlockState state = player.serverLevel().getBlockState(scanned);
                    if (state.getBlock() instanceof FarmBlock && state.getValue(FarmBlock.MOISTURE) < FULL_MOISTURE) {
                        castData.addDryFarmland(scanned);
                        castData.addRainTarget(scanned);
                    } else if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                        castData.addGrowableCrop(scanned);
                        castData.addRainTarget(scanned);
                    }
                }
            }
        }
        return castData;
    }
}
