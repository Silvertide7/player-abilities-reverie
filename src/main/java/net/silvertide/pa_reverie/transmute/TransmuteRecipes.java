package net.silvertide.pa_reverie.transmute;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class TransmuteRecipes {

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, PAReverie.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, PAReverie.MOD_ID);

    public static final Supplier<RecipeType<TransmuteRecipe>> TRANSMUTE_TYPE =
            RECIPE_TYPES.register("transmute", () -> new RecipeType<TransmuteRecipe>() {
                @Override
                public String toString() {
                    return PAReverie.MOD_ID + ":transmute";
                }
            });

    public static final Supplier<RecipeSerializer<TransmuteRecipe>> TRANSMUTE_SERIALIZER =
            RECIPE_SERIALIZERS.register("transmute", TransmuteRecipe.Serializer::new);

    private TransmuteRecipes() {}

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    @Nullable
    public static TransmuteRecipe findBest(Level level, ItemStack held, int spellLevel) {
        SingleRecipeInput input = new SingleRecipeInput(held);
        TransmuteRecipe best = null;
        for (RecipeHolder<TransmuteRecipe> holder : level.getRecipeManager().getAllRecipesFor(TRANSMUTE_TYPE.get())) {
            TransmuteRecipe recipe = holder.value();
            if (recipe.level() <= spellLevel && recipe.matches(input, level)) {
                if (best == null || recipe.level() > best.level()) {
                    best = recipe;
                }
            }
        }
        return best;
    }
}
