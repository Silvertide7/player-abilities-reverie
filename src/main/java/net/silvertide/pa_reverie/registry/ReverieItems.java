package net.silvertide.pa_reverie.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.item.EphemeralFoodItem;

import java.util.function.Supplier;

public final class ReverieItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PAReverie.MOD_ID);

    public static final DeferredItem<EphemeralFoodItem> EPHEMERAL_BISCUIT = register(
            "ephemeral_biscuit", UseAnim.EAT, 3000,
            () -> new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationModifier(1.2f)
                    .build());

    public static final DeferredItem<EphemeralFoodItem> EPHEMERAL_NECTAR = register(
            "ephemeral_nectar", UseAnim.DRINK, 6000,
            () -> new FoodProperties.Builder()
                    .nutrition(10)
                    .saturationModifier(1.4f)
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 200), 1.0f)
                    .build());

    public static final DeferredItem<EphemeralFoodItem> EPHEMERAL_FEAST = register(
            "ephemeral_feast", UseAnim.EAT, 15000,
            () -> new FoodProperties.Builder()
                    .nutrition(14)
                    .saturationModifier(1.6f)
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 300), 1.0f)
                    .effect(new MobEffectInstance(MobEffects.ABSORPTION, 1200), 1.0f)
                    .build());

    private static DeferredItem<EphemeralFoodItem> register(String name, UseAnim useAnimation,
                                                            int defaultLifetimeTicks, Supplier<FoodProperties> food) {
        return ITEMS.registerItem(name, properties ->
                new EphemeralFoodItem(properties.food(food.get()).stacksTo(16), useAnimation, defaultLifetimeTicks));
    }

    private ReverieItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
