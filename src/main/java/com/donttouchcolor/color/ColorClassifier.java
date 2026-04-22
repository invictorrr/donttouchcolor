package com.donttouchcolor.color;

import com.donttouchcolor.config.DontTouchColorConfig;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ColorClassifier {
    private final DontTouchColorConfig config;
    private final Map<Item, GameColor> itemCache = new HashMap<>();

    /**
     * Static color assignments for vanilla animals.
     * Config overrides take priority over this table.
     */
    private static final Map<String, GameColor> ANIMAL_COLORS;
    static {
        Map<String, GameColor> m = new HashMap<>();
        // Brown / earthy
        m.put("minecraft:cow",            GameColor.BROWN);
        m.put("minecraft:rabbit",         GameColor.BROWN);
        m.put("minecraft:horse",          GameColor.BROWN);
        m.put("minecraft:donkey",         GameColor.BROWN);
        m.put("minecraft:mule",           GameColor.BROWN);
        m.put("minecraft:goat",           GameColor.BROWN);
        // Red
        m.put("minecraft:mooshroom",      GameColor.RED);
        m.put("minecraft:pig",            GameColor.RED);
        m.put("minecraft:strider",        GameColor.RED);
        m.put("minecraft:axolotl",        GameColor.RED);
        m.put("minecraft:salmon",         GameColor.RED);
        // Yellow / orange
        m.put("minecraft:chicken",        GameColor.YELLOW);
        m.put("minecraft:llama",          GameColor.YELLOW);
        m.put("minecraft:trader_llama",   GameColor.YELLOW);
        m.put("minecraft:fox",            GameColor.YELLOW);
        m.put("minecraft:bee",            GameColor.YELLOW);
        m.put("minecraft:ocelot",         GameColor.YELLOW);
        m.put("minecraft:camel",          GameColor.YELLOW);
        m.put("minecraft:tropical_fish",  GameColor.YELLOW);
        m.put("minecraft:pufferfish",     GameColor.YELLOW);
        m.put("minecraft:cod",            GameColor.YELLOW);
        m.put("minecraft:sniffer",        GameColor.YELLOW);
        // Green
        m.put("minecraft:turtle",         GameColor.GREEN);
        m.put("minecraft:frog",           GameColor.GREEN);
        // Unknown — no dominant single color
        m.put("minecraft:sheep",          GameColor.UNKNOWN); // handled specially below
        m.put("minecraft:wolf",           GameColor.UNKNOWN);
        m.put("minecraft:cat",            GameColor.UNKNOWN);
        m.put("minecraft:panda",          GameColor.UNKNOWN);
        m.put("minecraft:polar_bear",     GameColor.UNKNOWN);
        m.put("minecraft:bat",            GameColor.UNKNOWN);
        m.put("minecraft:squid",          GameColor.UNKNOWN);
        m.put("minecraft:glow_squid",     GameColor.UNKNOWN);
        ANIMAL_COLORS = Collections.unmodifiableMap(m);
    }

    public ColorClassifier(DontTouchColorConfig config) {
        this.config = config;
    }

    public void clearCache() {
        itemCache.clear();
    }

    public GameColor classifyItem(Item item) {
        if (itemCache.containsKey(item)) {
            return itemCache.get(item);
        }
        Identifier id = Registries.ITEM.getId(item);
        GameColor override = config.getItemOverrides().get(id.toString());
        if (override != null) {
            itemCache.put(item, override);
            return override;
        }

        GameColor color;
        if (item instanceof BlockItem blockItem) {
            color = classifyBlock(blockItem.getBlock());
        } else {
            color = classifyByName(id.getPath());
        }
        itemCache.put(item, color);
        return color;
    }

    public GameColor classifyBlock(Block block) {
        MapColor mapColor = block.getDefaultMapColor();
        int rgb = mapColor.getRenderColor(MapColor.Brightness.NORMAL);
        return nearestColor(rgb);
    }

    public GameColor classifyEntity(Entity entity) {
        String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();

        // Config overrides have highest priority
        GameColor override = config.getEntityOverrides().get(id);
        if (override != null) {
            return override;
        }

        // Sheep: classify by the actual wool dye color
        if (entity instanceof SheepEntity sheep) {
            return dyeToGameColor(sheep.getColor());
        }

        // Static table for known vanilla animals
        GameColor known = ANIMAL_COLORS.get(id);
        if (known != null) {
            return known;
        }

        // Fallback: name heuristics (covers modded entities)
        return classifyByName(Registries.ENTITY_TYPE.getId(entity.getType()).getPath());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private GameColor classifyByName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("yellow") || lower.contains("gold")) {
            return GameColor.YELLOW;
        }
        if (lower.contains("blue")) {
            return GameColor.BLUE;
        }
        if (lower.contains("red") || lower.contains("crimson")) {
            return GameColor.RED;
        }
        if (lower.contains("green")) {
            return GameColor.GREEN;
        }
        if (lower.contains("brown") || lower.contains("oak") || lower.contains("spruce")) {
            return GameColor.BROWN;
        }
        return GameColor.UNKNOWN;
    }

    /**
     * Maps a Minecraft DyeColor to the nearest GameColor category.
     * Achromatic dyes (white, gray, black) return UNKNOWN.
     */
    private GameColor dyeToGameColor(DyeColor dye) {
        return switch (dye) {
            case YELLOW                   -> GameColor.YELLOW;
            case ORANGE                   -> GameColor.YELLOW;  // orange ≈ yellow
            case BLUE, LIGHT_BLUE, CYAN   -> GameColor.BLUE;
            case RED, MAGENTA, PINK       -> GameColor.RED;
            case GREEN, LIME              -> GameColor.GREEN;
            case BROWN                    -> GameColor.BROWN;
            case PURPLE                   -> GameColor.BLUE;    // purple ≈ blue
            default                       -> GameColor.UNKNOWN; // white, gray, light_gray, black
        };
    }

    /**
     * Returns the nearest GameColor using Euclidean RGB distance.
     *
     * Achromatic colors (low saturation range, i.e. gray/white/black)
     * return UNKNOWN because they have no dominant hue.
     */
    private GameColor nearestColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // If max−min < threshold the color is nearly gray → no dominant hue
        int satRange = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
        if (satRange < 40) {
            return GameColor.UNKNOWN;
        }

        GameColor best = GameColor.UNKNOWN;
        double bestDistanceSq = Double.MAX_VALUE;
        for (GameColor color : new GameColor[]{GameColor.YELLOW, GameColor.BLUE, GameColor.RED, GameColor.GREEN, GameColor.BROWN}) {
            int cr = (color.rgb() >> 16) & 0xFF;
            int cg = (color.rgb() >> 8) & 0xFF;
            int cb = color.rgb() & 0xFF;
            double distSq = Math.pow(r - cr, 2) + Math.pow(g - cg, 2) + Math.pow(b - cb, 2);
            if (distSq < bestDistanceSq) {
                bestDistanceSq = distSq;
                best = color;
            }
        }
        return best;
    }
}
