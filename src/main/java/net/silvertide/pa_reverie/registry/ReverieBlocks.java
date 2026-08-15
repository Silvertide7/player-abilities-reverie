package net.silvertide.pa_reverie.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.block.DryAirBlock;

public final class ReverieBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, PAReverie.MOD_ID);

    public static final RegistryObject<DryAirBlock> DRY_AIR = BLOCKS.register("dry_air", () -> new DryAirBlock(
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
