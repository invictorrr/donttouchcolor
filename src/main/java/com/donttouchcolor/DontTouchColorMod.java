package com.donttouchcolor;

import com.donttouchcolor.color.ColorClassifier;
import com.donttouchcolor.command.DontTouchColorCommand;
import com.donttouchcolor.config.DontTouchColorConfig;
import com.donttouchcolor.item.ProtectiveGloveItem;
import com.donttouchcolor.recipe.GloveRecipe;
import com.donttouchcolor.recipe.GloveRecipeEditor;
import com.donttouchcolor.rules.DeathRules;
import com.donttouchcolor.state.ColorGameState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.Item;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DontTouchColorMod implements ModInitializer {
    public static final String MOD_ID = "donttouchcolor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DontTouchColorConfig CONFIG = new DontTouchColorConfig();
    public static final ColorGameState STATE = new ColorGameState();
    public static final GloveRecipeEditor GLOVE_RECIPE_EDITOR = new GloveRecipeEditor();

    private static final Identifier PROTECTIVE_GLOVE_ID = Identifier.of(MOD_ID, "protective_glove");
    private static final RegistryKey<Item> PROTECTIVE_GLOVE_KEY = RegistryKey.of(RegistryKeys.ITEM, PROTECTIVE_GLOVE_ID);
    public static final Item PROTECTIVE_GLOVE = Registry.register(Registries.ITEM, PROTECTIVE_GLOVE_ID,
        new ProtectiveGloveItem(new Item.Settings().registryKey(PROTECTIVE_GLOVE_KEY).maxCount(1)));

    /**
     * Serializer for the dynamic glove crafting recipe.
     * Registered as "donttouchcolor:glove"; referenced by
     * data/donttouchcolor/recipe/protective_glove.json.
     */
    public static final SpecialCraftingRecipe.SpecialRecipeSerializer<GloveRecipe> GLOVE_RECIPE_SERIALIZER =
        Registry.register(Registries.RECIPE_SERIALIZER,
            Identifier.of(MOD_ID, "glove"),
            new SpecialCraftingRecipe.SpecialRecipeSerializer<>(GloveRecipe::new));

    private static final ColorClassifier CLASSIFIER = new ColorClassifier(CONFIG);
    private static final DeathRules DEATH_RULES = new DeathRules(STATE, CLASSIFIER);

    @Override
    public void onInitialize() {
        CONFIG.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            DontTouchColorCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CLASSIFIER.clearCache());

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            STATE.tick(server);
            for (var player : server.getPlayerManager().getPlayerList()) {
                DEATH_RULES.checkPlayer(player);
            }
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                DEATH_RULES.onDirectAnimalTouch(serverPlayer, entity);
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                DEATH_RULES.onDirectAnimalTouch(serverPlayer, entity);
            }
            return ActionResult.PASS;
        });

        // Free the recipe-editor inventory when a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            GLOVE_RECIPE_EDITOR.clearPlayer(handler.player.getUuid()));

        LOGGER.info("DontTouchColor initialized");
    }
}
