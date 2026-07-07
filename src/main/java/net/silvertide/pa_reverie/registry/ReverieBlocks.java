package net.silvertide.pa_reverie.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.silvertide.pa_reverie.block.DryAirBlock;

public final class ReverieBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PAReverie.MOD_ID);

    public static final DeferredBlock<DryAirBlock> DRY_AIR = BLOCKS.register("dry_air", () -> new DryAirBlock(
            BlockBehaviour.Properties.of()
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .instabreak()
                    .noLootTable()
                    .sound(SoundType.EMPTY)
                    .lightLevel(state -> 0)
                    .pushReaction(PushReaction.DESTROY)));

    private ReverieBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
