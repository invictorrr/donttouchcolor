package com.donttouchcolor.recipe;

import com.donttouchcolor.config.DontTouchColorConfig;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GloveRecipeEditor {
    /**
     * Inventory layout — GENERIC_9X3 (27 slots, 3 rows of 9).
     * The recipe occupies the left 3×3 block:
     *
     *   [0][1][2] [ ][ ][ ][ ][ ][ ]   ← row 0
     *   [9][10][11][ ][ ][ ][ ][ ][ ]  ← row 1
     *   [18][19][20][ ][ ][ ][ ][ ][ ] ← row 2
     *
     * recipe[0..2] → slots 0,1,2
     * recipe[3..5] → slots 9,10,11
     * recipe[6..8] → slots 18,19,20
     */
    private static final int[] RECIPE_SLOTS = {0, 1, 2, 9, 10, 11, 18, 19, 20};

    private final Map<UUID, SimpleInventory> openEditors = new HashMap<>();

    public void open(ServerPlayerEntity player, DontTouchColorConfig config) {
        SimpleInventory inventory = new SimpleInventory(27);
        List<String> recipe = config.getGloveRecipe();

        for (int i = 0; i < 9; i++) {
            String id = recipe.get(i);
            if (id == null || id.isBlank() || "minecraft:air".equals(id)) {
                continue;
            }
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null && Registries.ITEM.containsId(identifier)) {
                inventory.setStack(RECIPE_SLOTS[i], new ItemStack(Registries.ITEM.get(identifier)));
            }
        }

        openEditors.put(player.getUuid(), inventory);
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, p) ->
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3),
            Text.literal("Receta del Guante  (3×3 columna izquierda)")));
    }

    /**
     * Reads the 3×3 recipe slots from the open editor and persists to config.
     * Returns false if the player has no editor open.
     */
    public boolean update(ServerPlayerEntity player, DontTouchColorConfig config) {
        SimpleInventory inv = openEditors.get(player.getUuid());
        if (inv == null) {
            return false;
        }
        List<String> serialized = new ArrayList<>(9);
        for (int recipeSlot : RECIPE_SLOTS) {
            ItemStack stack = inv.getStack(recipeSlot);
            serialized.add(stack.isEmpty() ? "" : Registries.ITEM.getId(stack.getItem()).toString());
        }
        config.setGloveRecipe(serialized);
        config.save();
        return true;
    }

    /** Called on player disconnect to free the cached inventory. */
    public void clearPlayer(UUID uuid) {
        openEditors.remove(uuid);
    }

    public boolean tryCraftFromGrid(List<ItemStack> matrix, DontTouchColorConfig config) {
        return config.recipeMatches(matrix);
    }
}
