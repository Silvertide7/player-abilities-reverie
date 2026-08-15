package net.silvertide.pa_reverie.transmute;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class TransmuteRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputCount;
    private final ItemStack result;
    private final int maxConversions;
    private final int level;
    private final int cooldown;

    public TransmuteRecipe(ResourceLocation id, Ingredient input, int inputCount, ItemStack result,
                           int maxConversions, int level, int cooldown) {
        this.id = id;
        this.input = input;
        this.inputCount = inputCount;
        this.result = result;
        this.maxConversions = maxConversions;
        this.level = level;
        this.cooldown = cooldown;
    }

    public Ingredient input() {
        return input;
    }

    public int inputCount() {
        return inputCount;
    }

    public ItemStack result() {
        return result;
    }

    public int maxConversions() {
        return maxConversions;
    }

    public int level() {
        return level;
    }

    public int cooldown() {
        return cooldown;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TransmuteRecipes.TRANSMUTE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TransmuteRecipes.TRANSMUTE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<TransmuteRecipe> {

        @Override
        public TransmuteRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new TransmuteRecipe(recipeId,
                    Ingredient.fromJson(json.get("ingredient"), false),
                    atLeast(json, "input_count", 1),
                    resultFromJson(json),
                    atLeast(json, "max_conversions", 1),
                    atLeast(json, "level", 1),
                    atLeast(json, "cooldown", 0));
        }

        private static int atLeast(JsonObject json, String field, int minimum) {
            int value = GsonHelper.getAsInt(json, field, minimum);
            if (value < minimum) {
                throw new JsonSyntaxException(field + " must be at least " + minimum + ", was " + value);
            }
            return value;
        }

        private static ItemStack resultFromJson(JsonObject json) {
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            if (result.isEmpty()) {
                throw new JsonSyntaxException("result must produce at least one item");
            }
            return result;
        }

        @Override
        public TransmuteRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            return new TransmuteRecipe(recipeId,
                    Ingredient.fromNetwork(buf),
                    buf.readVarInt(),
                    buf.readItem(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TransmuteRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeVarInt(recipe.inputCount);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.maxConversions);
            buf.writeVarInt(recipe.level);
            buf.writeVarInt(recipe.cooldown);
        }
    }
}
