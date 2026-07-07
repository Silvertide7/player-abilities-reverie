package net.silvertide.pa_reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DryAirBlock extends Block implements LiquidBlockContainer {

    public static final int MAX_WARD_COUNT = 15;
    public static final IntegerProperty WARD_COUNT = IntegerProperty.create("ward_count", 1, MAX_WARD_COUNT);

    public DryAirBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WARD_COUNT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WARD_COUNT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return player != null;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        level.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL);
        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int wardCount = state.getValue(WARD_COUNT);
        if (wardCount > 1) {
            level.setBlock(pos, state.setValue(WARD_COUNT, wardCount - 1), Block.UPDATE_CLIENTS);
        } else {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        }
    }
}
