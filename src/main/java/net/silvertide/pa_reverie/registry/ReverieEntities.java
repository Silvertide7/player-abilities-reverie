package net.silvertide.pa_reverie.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.entity.EscapeShaftRiseEntity;

public final class ReverieEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PAReverie.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EscapeShaftRiseEntity>> ESCAPE_SHAFT_RISE =
            ENTITY_TYPES.register("escape_shaft_rise", () -> EntityType.Builder
                    .<EscapeShaftRiseEntity>of(EscapeShaftRiseEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .noSummon()
                    .updateInterval(1)
                    .build("escape_shaft_rise"));

    private ReverieEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
