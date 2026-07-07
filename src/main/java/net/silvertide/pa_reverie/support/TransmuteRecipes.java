package net.silvertide.pa_reverie.support;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class TransmuteRecipes {
    public record TransmuteRecipe(Item input, int inputCount, Item result, int resultCount,
                                  int maxConversions, int level, int cooldownSeconds) {
    }

    private static final List<TransmuteRecipe> RECIPES = List.of(
            new TransmuteRecipe(Items.COPPER_INGOT, 4, Items.IRON_INGOT, 1, 8, 1, 120),
            new TransmuteRecipe(Items.IRON_INGOT, 4, Items.GOLD_INGOT, 1, 8, 2, 300),
            new TransmuteRecipe(Items.GOLD_INGOT, 8, Items.DIAMOND, 1, 4, 3, 900));

    private TransmuteRecipes() {
    }

    public static TransmuteRecipe findBestMatch(ItemStack held, int spellLevel) {
        TransmuteRecipe best = null;
        for (TransmuteRecipe recipe : RECIPES) {
            if (recipe.level() <= spellLevel && held.is(recipe.input()) && held.getCount() >= recipe.inputCount()
                    && (best == null || recipe.level() > best.level())) {
                best = recipe;
            }
        }
        return best;
    }

    public static ItemStack resultFor(TransmuteRecipe recipe, int batches) {
        return new ItemStack(recipe.result(), recipe.resultCount() * batches);
    }
}
