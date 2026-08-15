package net.silvertide.pa_reverie.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.compat.FarmersDelightCompat;
import net.silvertide.pa_reverie.item.EphemeralFoodItem;

import java.util.List;
import java.util.function.Supplier;

public final class ConjuredFoods {

    private static final int TICKS_PER_MINUTE = 20 * 60;

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PAReverie.MOD_ID);

    public static final RegistryObject<EphemeralFoodItem> EPHEMERAL_BISCUIT = register(
            "ephemeral_biscuit", UseAnim.EAT, 1 * TICKS_PER_MINUTE,
            () -> FarmersDelightCompat.withNourishment(new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationMod(1.2f), 2 * TICKS_PER_MINUTE)
                    .build());

    public static final RegistryObject<EphemeralFoodItem> EPHEMERAL_NECTAR = register(
            "ephemeral_nectar", UseAnim.DRINK, 2 * TICKS_PER_MINUTE,
            () -> FarmersDelightCompat.withNourishment(new FoodProperties.Builder()
                    .nutrition(12)
                    .saturationMod(1.5f), 3 * TICKS_PER_MINUTE)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, TICKS_PER_MINUTE), 1.0f)
                    .build());

    public static final RegistryObject<EphemeralFoodItem> EPHEMERAL_FEAST = register(
            "ephemeral_feast", UseAnim.EAT, 3 * TICKS_PER_MINUTE,
            () -> FarmersDelightCompat.withNourishment(new FoodProperties.Builder()
                    .nutrition(16)
                    .saturationMod(2.0f)
                    .alwaysEat(), 4 * TICKS_PER_MINUTE)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, TICKS_PER_MINUTE), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TICKS_PER_MINUTE), 1.0f)
                    .build());

    public static final List<RegistryObject<EphemeralFoodItem>> BY_TIER =
            List.of(EPHEMERAL_BISCUIT, EPHEMERAL_NECTAR, EPHEMERAL_FEAST);

    private static RegistryObject<EphemeralFoodItem> register(String name, UseAnim useAnimation, int lifetimeTicks, Supplier<FoodProperties> food) {
        return ITEMS.register(name, () -> new EphemeralFoodItem(
                new Item.Properties().stacksTo(1).food(food.get()), useAnimation, lifetimeTicks));
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
