package com.donttouchcolor.config;

import com.donttouchcolor.color.GameColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DontTouchColorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type OVERRIDE_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private static final List<String> DEFAULT_RECIPE = Arrays.asList(
        "minecraft:leather", "minecraft:string", "minecraft:leather",
        "", "minecraft:iron_ingot", "",
        "", "", ""
    );

    private final Path path;
    private int randomTimerSeconds = 60;
    private final List<String> gloveRecipe = new ArrayList<>();
    private final Map<String, GameColor> itemOverrides = new HashMap<>();
    private final Map<String, GameColor> entityOverrides = new HashMap<>();

    public DontTouchColorConfig() {
        this.path = FabricLoader.getInstance().getConfigDir().resolve("donttouchcolor.json");
    }

    public void load() {
        if (!Files.exists(path)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            RawConfig raw = GSON.fromJson(reader, RawConfig.class);
            if (raw == null) {
                save();
                return;
            }
            randomTimerSeconds = Math.max(5, raw.randomTimerSeconds);
            applyRecipe(raw.gloveRecipe);
            applyOverrides(raw.itemOverrides, itemOverrides);
            applyOverrides(raw.entityOverrides, entityOverrides);
        } catch (IOException ignored) {
        }
    }

    private void applyRecipe(List<String> recipe) {
        gloveRecipe.clear();
        if (recipe == null) {
            return;
        }
        for (String id : recipe) {
            gloveRecipe.add(id == null ? "" : id);
        }
        while (gloveRecipe.size() < 9) {
            gloveRecipe.add("");
        }
        if (gloveRecipe.size() > 9) {
            gloveRecipe.subList(9, gloveRecipe.size()).clear();
        }
    }

    private void applyOverrides(Map<String, String> raw, Map<String, GameColor> target) {
        target.clear();
        if (raw == null) {
            return;
        }
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            target.put(entry.getKey(), GameColor.fromCommand(entry.getValue()));
        }
    }

    public void save() {
        RawConfig raw = new RawConfig();
        raw.randomTimerSeconds = randomTimerSeconds;
        raw.gloveRecipe = new ArrayList<>(gloveRecipe.isEmpty() ? DEFAULT_RECIPE : gloveRecipe);
        raw.itemOverrides = stringifyOverrides(itemOverrides);
        raw.entityOverrides = stringifyOverrides(entityOverrides);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(raw, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private Map<String, String> stringifyOverrides(Map<String, GameColor> source) {
        Map<String, String> raw = new HashMap<>();
        for (Map.Entry<String, GameColor> entry : source.entrySet()) {
            raw.put(entry.getKey(), entry.getValue().commandName());
        }
        return raw;
    }

    public int getRandomTimerSeconds() {
        return randomTimerSeconds;
    }

    public void setRandomTimerSeconds(int seconds) {
        this.randomTimerSeconds = Math.max(5, seconds);
    }

    /** Returns a 9-element copy of the recipe (never mutates internal state). */
    public List<String> getGloveRecipe() {
        if (gloveRecipe.isEmpty()) {
            return new ArrayList<>(DEFAULT_RECIPE);
        }
        return new ArrayList<>(gloveRecipe);
    }

    public void setGloveRecipe(List<String> ids) {
        gloveRecipe.clear();
        gloveRecipe.addAll(ids);
        while (gloveRecipe.size() < 9) {
            gloveRecipe.add("");
        }
        if (gloveRecipe.size() > 9) {
            gloveRecipe.subList(9, gloveRecipe.size()).clear();
        }
    }

    public Map<String, GameColor> getItemOverrides() {
        return itemOverrides;
    }

    public Map<String, GameColor> getEntityOverrides() {
        return entityOverrides;
    }

    public boolean recipeMatches(List<ItemStack> matrix) {
        List<String> recipe = getGloveRecipe();
        if (matrix.size() < 9) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            String expected = recipe.get(i);
            ItemStack stack = matrix.get(i);
            if (expected == null || expected.isBlank() || "minecraft:air".equals(expected)) {
                if (!stack.isEmpty()) {
                    return false;
                }
                continue;
            }
            Identifier id = Identifier.tryParse(expected);
            if (id == null) {
                return false;
            }
            Item item = Registries.ITEM.get(id);
            if (stack.isEmpty() || stack.getItem() != item) {
                return false;
            }
        }
        return true;
    }

    private static final class RawConfig {
        int randomTimerSeconds = 60;
        List<String> gloveRecipe = new ArrayList<>();
        Map<String, String> itemOverrides = GSON.fromJson("{}", OVERRIDE_TYPE);
        Map<String, String> entityOverrides = GSON.fromJson("{}", OVERRIDE_TYPE);
    }
}
