package net.silvertide.pa_reverie.compat;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.fml.ModList;
import vectorwing.farmersdelight.common.registry.ModEffects;

public final class FarmersDelightCompat {
    private static final boolean FARMERS_DELIGHT_LOADED = ModList.get().isLoaded("farmersdelight");

    private FarmersDelightCompat() {
    }

    public static FoodProperties.Builder withNourishment(FoodProperties.Builder builder, int durationTicks) {
        return FARMERS_DELIGHT_LOADED ? Bridge.withNourishment(builder, durationTicks) : builder;
    }

    private static final class Bridge {
        private static FoodProperties.Builder withNourishment(FoodProperties.Builder builder, int durationTicks) {
            return builder.effect(new MobEffectInstance(ModEffects.NOURISHMENT, durationTicks), 1.0f);
        }
    }
}
