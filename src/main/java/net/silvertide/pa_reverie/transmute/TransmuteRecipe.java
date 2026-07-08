package net.silvertide.pa_reverie.transmute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class TransmuteRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient input;
    private final int inputCount;
    private final ItemStack result;
    private final int maxConversions;
    private final int level;
    private final int cooldown;

    public TransmuteRecipe(Ingredient input, int inputCount, ItemStack result, int maxConversions, int level, int cooldown) {
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
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
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

        private final MapCodec<TransmuteRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.input),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("input_count", 1).forGetter(recipe -> recipe.inputCount),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("max_conversions", 1).forGetter(recipe -> recipe.maxConversions),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("level", 1).forGetter(recipe -> recipe.level),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("cooldown", 0).forGetter(recipe -> recipe.cooldown)
        ).apply(instance, TransmuteRecipe::new));

        private final StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> streamCodec = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.input,
                ByteBufCodecs.VAR_INT, recipe -> recipe.inputCount,
                ItemStack.STREAM_CODEC, recipe -> recipe.result,
                ByteBufCodecs.VAR_INT, recipe -> recipe.maxConversions,
                ByteBufCodecs.VAR_INT, recipe -> recipe.level,
                ByteBufCodecs.VAR_INT, recipe -> recipe.cooldown,
                TransmuteRecipe::new
        );

        @Override
        public MapCodec<TransmuteRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
