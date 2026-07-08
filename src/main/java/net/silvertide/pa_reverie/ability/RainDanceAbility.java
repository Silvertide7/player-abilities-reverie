package net.silvertide.pa_reverie.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.silvertide.pa_reverie.support.RainDanceCastData;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityUseType;

import java.util.List;

public final class RainDanceAbility extends HarvestAbility {
    private static final int COOLDOWN_SECONDS = 1800;
    private static final int BASE_SPELL_POWER = 4;
    private static final int SPELL_POWER_PER_LEVEL = 4;
    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS = 24;
    private static final int SCAN_LAYERS_BELOW_FEET = 2;
    private static final int RAIN_RAMP_TICKS = 100;
    private static final int FARMLAND_WET_PER_TICK = 16;
    private static final int[] CROPS_GROWN_PER_TICK_BY_LEVEL = {1, 4, 9};
    private static final int MOISTURE_FULL = 7;
    private static final int RAIN_DROPS_AT_FULL = 12;
    private static final double RAIN_SPAWN_HEIGHT_ABOVE_HEAD = 3.0;
    private static final double RAIN_HORIZONTAL_JITTER = 0.25;
    private static final int RAIN_SOUND_INTERVAL_TICKS = 35;
    private static final IntegerProperty[] CROP_AGE_PROPERTIES = {
            BlockStateProperties.AGE_7,
            BlockStateProperties.AGE_5,
            BlockStateProperties.AGE_3,
            BlockStateProperties.AGE_2,
            BlockStateProperties.AGE_1
    };

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
        RainDanceCastData survey = new RainDanceCastData();
        scanField(player.level(), player.blockPosition(), fieldRadius(player, level), survey);
        return survey.foundAnything();
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, int level) {
        return Component.translatable("message.pa_reverie.rain_dance_no_field");
    }

    @Override
    public void onUseTick(ServerPlayer player, int level, int elapsedTicks, int totalTicks) {
        ServerLevel serverLevel = player.serverLevel();
        int radius = fieldRadius(player, level);
        RainDanceCastData data;
        if (AbilityAPI.getUseData(player) instanceof RainDanceCastData existing) {
            data = existing;
            if (!data.hasWork()) {
                AbilityAPI.finishUse(player);
                return;
            }
        } else {
            data = new RainDanceCastData();
            AbilityAPI.setUseData(player, data);
            scanField(serverLevel, player.blockPosition(), radius, data);
        }
        int elapsed = data.incrementElapsedTicks();
        float rainIntensity = Math.min(1.0f, (float) elapsed / RAIN_RAMP_TICKS);
        spawnRainEffects(serverLevel, data, player, rainIntensity, elapsed);
        irrigateFarmland(serverLevel, data);
        growCrops(serverLevel, data, cropsGrownPerTick(player, level));
    }

    @Override
    public void onUseComplete(ServerPlayer player, int level, boolean cancelled) {
        if (cancelled && !player.isCreative()
                && AbilityAPI.getActiveUse(player).map(use -> use.getElapsedTicks() > 0).orElse(false)) {
            AbilityAPI.setCooldown(player, this, getCooldownTicks(level));
        }
    }

    private void scanField(Level level, BlockPos center, int radius, RainDanceCastData data) {
        int radiusSquared = radius * radius;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -SCAN_LAYERS_BELOW_FEET; dy <= 0; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radiusSquared) {
                        continue;
                    }
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.getBlock() instanceof FarmBlock) {
                        BlockPos fieldPos = cursor.immutable();
                        data.addRainTarget(fieldPos);
                        if (state.getValue(FarmBlock.MOISTURE) < MOISTURE_FULL) {
                            data.addDryFarmland(fieldPos);
                        }
                    } else if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                        BlockPos fieldPos = cursor.immutable();
                        data.addRainTarget(fieldPos);
                        data.addGrowableCrop(fieldPos);
                    }
                }
            }
        }
    }

    private void irrigateFarmland(ServerLevel level, RainDanceCastData data) {
        for (int i = 0; i < FARMLAND_WET_PER_TICK; i++) {
            BlockPos pos = data.pollDryFarmland();
            if (pos == null) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof FarmBlock) {
                level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, MOISTURE_FULL), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void growCrops(ServerLevel level, RainDanceCastData data, int cropsToTick) {
        for (int i = 0; i < cropsToTick; i++) {
            if (!tickOneCrop(level, data)) {
                return;
            }
        }
    }

    private boolean tickOneCrop(ServerLevel level, RainDanceCastData data) {
        BlockPos pos = data.takeNextCropTurn(level.random);
        if (pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
            BlockState grown = grownByOneStage(state);
            if (grown != state) {
                level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
                if (grown.getBlock() instanceof CropBlock grownCrop && !grownCrop.isMaxAge(grown)) {
                    data.keepForNextRound(pos);
                }
            }
        }
        return true;
    }

    private static BlockState grownByOneStage(BlockState state) {
        for (IntegerProperty ageProperty : CROP_AGE_PROPERTIES) {
            if (state.hasProperty(ageProperty)) {
                int age = state.getValue(ageProperty);
                int maxAge = ageProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(age);
                if (age < maxAge) {
                    return state.setValue(ageProperty, age + 1);
                }
            }
        }
        return state;
    }

    private void spawnRainEffects(ServerLevel level, RainDanceCastData data, ServerPlayer player,
                                  float rainIntensity, int elapsed) {
        List<BlockPos> rainTargets = data.getRainTargets();
        if (!rainTargets.isEmpty()) {
            double cloudY = player.getY() + player.getBbHeight() + RAIN_SPAWN_HEIGHT_ABOVE_HEAD;
            int drops = Math.round(RAIN_DROPS_AT_FULL * rainIntensity);
            for (int i = 0; i < drops; i++) {
                BlockPos target = rainTargets.get(level.random.nextInt(rainTargets.size()));
                level.sendParticles(ParticleTypes.FALLING_WATER,
                        target.getX() + 0.5, cloudY, target.getZ() + 0.5,
                        1, RAIN_HORIZONTAL_JITTER, 0.0, RAIN_HORIZONTAL_JITTER, 0.0);
            }
        }
        if (elapsed % RAIN_SOUND_INTERVAL_TICKS == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.6f * rainIntensity, 1.0f);
        }
    }

    private int fieldRadius(ServerPlayer player, int level) {
        return Math.clamp(Math.round(spellPower(player, BASE_SPELL_POWER, SPELL_POWER_PER_LEVEL, level)),
                MIN_RADIUS, MAX_RADIUS);
    }

    private int cropsGrownPerTick(ServerPlayer player, int level) {
        return net.silvertide.pa_reverie.support.AbilityPower.scaled(player,
                byLevel(level, CROPS_GROWN_PER_TICK_BY_LEVEL));
    }
}
