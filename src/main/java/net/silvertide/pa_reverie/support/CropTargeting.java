package net.silvertide.pa_reverie.support;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.silvertide.pa_reverie.PAReverie;

public final class CropTargeting {

    public static final TagKey<Block> TARGETS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "verdant_cascade_targets")
    );

    private static final IntegerProperty[] KNOWN_AGE_PROPERTIES = {
            BlockStateProperties.AGE_7,
            BlockStateProperties.AGE_5,
            BlockStateProperties.AGE_3,
            BlockStateProperties.AGE_2,
            BlockStateProperties.AGE_1
    };

    private CropTargeting() {}

    public static boolean isHarvestable(BlockState state) {
        return isValidTarget(state) && isMature(state);
    }

    public static BlockState newlyPlantedStatePreservingProperties(BlockState mature) {
        if (mature.getBlock() instanceof CropBlock crop) {
            return crop.getStateForAge(0);
        }
        for (IntegerProperty ageProperty : KNOWN_AGE_PROPERTIES) {
            if (mature.hasProperty(ageProperty)) {
                return mature.setValue(ageProperty, 0);
            }
        }
        return mature.getBlock().defaultBlockState();
    }

    private static boolean isValidTarget(BlockState state) {
        return state.is(TARGETS);
    }

    private static boolean isMature(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        }
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= 2;
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        for (IntegerProperty ageProperty : KNOWN_AGE_PROPERTIES) {
            if (state.hasProperty(ageProperty)) {
                int maxAge = ageProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
                return state.getValue(ageProperty) >= maxAge;
            }
        }
        return false;
    }
}
