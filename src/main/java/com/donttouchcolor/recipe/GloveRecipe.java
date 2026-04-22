package com.donttouchcolor.recipe;

import com.donttouchcolor.DontTouchColorMod;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic crafting recipe for the Protective Glove.
 * The exact ingredients are read from {@link DontTouchColorMod#CONFIG} at
 * match-time, so admins can change the recipe in-game via
 * {@code /donttouchcolor gloverecipe} + {@code update} without restarting.
 */
public final class GloveRecipe extends SpecialCraftingRecipe {

    public GloveRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        // Build a flat 9-slot list; pad with EMPTY for grids smaller than 3×3
        List<ItemStack> matrix = new ArrayList<>(9);
        int size = input.size();
        for (int i = 0; i < 9; i++) {
            matrix.add(i < size ? input.getStackInSlot(i) : ItemStack.EMPTY);
        }
        return DontTouchColorMod.CONFIG.recipeMatches(matrix);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return DontTouchColorMod.PROTECTIVE_GLOVE.getDefaultStack();
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return DontTouchColorMod.GLOVE_RECIPE_SERIALIZER;
    }
}
