package net.silvertide.pa_reverie.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.item.EphemeralFoodItem;
import net.silvertide.pa_reverie.compat.FarmersDelightCompat;

import java.util.List;
import java.util.function.Supplier;

public final class ConjuredFoods {

    private static final int TICKS_PER_MINUTE = 20 * 60;

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(PAReverie.MOD_ID);

    public static final DeferredItem<EphemeralFoodItem> EPHEMERAL_BISCUIT = register(
            "ephemeral_biscuit", UseAnim.EAT, 1 * TICKS_PER_MINUTE,
            () -> FarmersDelightCompat.withNourishment(new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationModifier(1.2f), 2 * TICKS_PER_MINUTE)
                    .build());

    public static final DeferredItem<EphemeralFoodItem> EPHEMERAL_NECTAR = register(
            "ephemeral_nectar", UseAnim.DRINK, 2 * TICKS_PER_MINUTE,
            () -> FarmersDelightCompat.withNourishment(new FoodProperties.Builder()
                    .nutrition(12)
                    .saturationModifier(1.5f), 3 * TICKS_PER_MINUTE)
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, TICKS_PER_MINUTE), 1.0f)
                    .build());

    public static final DeferredItem<EphemeralFoodItem> EPHEMERAL_FEAST = register(
            "ephemeral_feast", UseAnim.EAT, 3 * TICKS_PER_MINUTE,
            () -> FarmersDelightCompat.withNourishment(new FoodProperties.Builder()
                    .nutrition(16)
                    .saturationModifier(2.0f)
                    .alwaysEdible(), 4 * TICKS_PER_MINUTE)
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, TICKS_PER_MINUTE), 1.0f)
                    .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TICKS_PER_MINUTE), 1.0f)
                    .build());

    public static final List<DeferredItem<EphemeralFoodItem>> BY_TIER =
            List.of(EPHEMERAL_BISCUIT, EPHEMERAL_NECTAR, EPHEMERAL_FEAST);

    private static DeferredItem<EphemeralFoodItem> register(String name, UseAnim useAnimation, int lifetimeTicks, Supplier<FoodProperties> food) {
        return ITEMS.registerItem(name, props -> new EphemeralFoodItem(props.stacksTo(1).food(food.get()), useAnimation, lifetimeTicks));
    }

    private ConjuredFoods() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(ConjuredFoods::addToFoodTab);
    }

    private static void addToFoodTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)) {
            BY_TIER.forEach(item -> event.accept(item.get()));
        }
    }
}
