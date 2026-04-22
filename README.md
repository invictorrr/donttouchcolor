# DontTouchColor

**Fabric mod for Minecraft 1.21.11**

A color-based death mod: one color is active at a time, and touching anything of that color kills you instantly.

---

## How it works

| Trigger | Kills you? | Glove protects? |
|---------|-----------|-----------------|
| Item/block of the active color in your inventory | Yes | Yes |
| Physically standing on or touching a block of the active color | Yes | No |
| Walking into or near an animal of the active color | Yes | No |
| Clicking / attacking an animal of the active color | Yes | No |

---

## Colors

| Command name | Color |
|---|---|
| `amarillo` | Yellow |
| `azul` | Blue (includes water) |
| `rojo` | Red |
| `verde` | Green |
| `marron` | Brown |

---

## Commands

```
/donttouchcolor <color> start             — Activate a fixed color (amarillo|azul|rojo|verde|marron)
/donttouchcolor random start              — Start random mode (color rotates on a timer)
/donttouchcolor stop                      — Deactivate the mod
/donttouchcolor options randomtimer <N>s  — Set seconds between color changes (min 5)
/donttouchcolor giveglove                 — Give yourself a Protective Glove
/donttouchcolor gloverecipe               — Open the glove recipe editor (3×3 left column)
/donttouchcolor gloverecipe update        — Save the current recipe editor layout
```

---

## Protective Glove

The **Protective Glove** lets you carry items of the active color without dying.
It does **not** protect you from standing on colored blocks or touching colored animals.

### Default recipe

```
[Leather] [String] [Leather]
[  Air  ] [Iron I] [  Air  ]
[  Air  ] [  Air  ] [  Air  ]
```

You can change the recipe in-game:
1. Run `/donttouchcolor gloverecipe` — a 3×3 editor opens (use the left 3 columns).
2. Place items in the grid to define the new recipe.
3. Run `/donttouchcolor gloverecipe update` to save.

The recipe takes effect immediately — no restart needed.

---

## Random mode & bossbar

- A bossbar shows the current active color to all players at all times.
- In random mode the color changes automatically every N seconds (default 60).
- Change the interval: `/donttouchcolor options randomtimer 30s`

---

## Configuration

Config file is saved at `<game_dir>/config/donttouchcolor.json`:

```json
{
  "randomTimerSeconds": 60,
  "gloveRecipe": ["minecraft:leather","minecraft:string","minecraft:leather","","minecraft:iron_ingot","","","",""],
  "itemOverrides": {},
  "entityOverrides": {}
}
```

### Color overrides

Force a specific item or entity to a given color:

```json
"itemOverrides": {
  "minecraft:oak_planks": "amarillo"
},
"entityOverrides": {
  "minecraft:zombie": "verde"
}
```

---

## Block color mapping

Blocks are classified by their `MapColor` using calibrated RGB anchors:

| Game color | Example blocks |
|---|---|
| `amarillo` | Gold block, sponge, glowstone, yellow wool, sand |
| `azul` | Water, blue wool, lapis block, cyan wool, light blue wool |
| `rojo` | Red wool, red concrete, nether brick, red mushroom block |
| `verde` | Grass, leaves, lime/green wool, emerald block, cactus, slime block |
| `marron` | Dirt, oak wood, spruce, brown mushroom, coarse dirt |

Gray, white and black blocks (`stone`, `snow`, `obsidian`, etc.) have no dominant hue and are classified as UNKNOWN — they never trigger death.

---

## Animal color mapping

| Game color | Animals |
|---|---|
| `amarillo` | Chicken, bee, fox, llama, ocelot, camel, tropical fish, pufferfish |
| `rojo` | Pig, mooshroom, strider, axolotl, salmon |
| `verde` | Turtle, frog |
| `marron` | Cow, rabbit, horse, donkey, mule, goat |
| Sheep | Classified by current wool color |

---

## Requirements

- Minecraft **1.21.11**
- Fabric Loader **>= 0.19.2**
- Fabric API **0.141.3+1.21.11**

---

## License

All Rights Reserved © invictorrr
