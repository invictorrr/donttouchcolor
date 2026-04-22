package com.donttouchcolor.color;

import net.minecraft.entity.boss.BossBar;

public enum GameColor {
    // RGB values are Euclidean-distance anchors used by ColorClassifier.nearestColor().
    // They are tuned to match Minecraft's actual MapColor render values (brightness NORMAL=220).
    //
    // Key calibration:
    //   MapColor.YELLOW  renders ~#C5C52C  →  YELLOW (0xE3D34E is a good anchor)
    //   MapColor.BLUE    renders ~#2C4199  →  BLUE   (0x3F55BD anchors all Minecraft blues)
    //   MapColor.RED     renders ~#842C2C  →  RED    (0x931B19 captures dark Minecraft reds)
    //   MapColor.GREEN   renders ~#586D2C  →  GREEN  (0x4A7D28 captures olive/dark greens)
    //   MapColor.OAK_TAN renders ~#7B663E  →  BROWN
    YELLOW("yellow", "amarillo", BossBar.Color.YELLOW, 0xE3D34E),
    BLUE  ("blue",   "azul",     BossBar.Color.BLUE,   0x3F55BD),
    RED   ("red",    "rojo",     BossBar.Color.RED,    0x931B19),
    GREEN ("green",  "verde",    BossBar.Color.GREEN,  0x4A7D28),
    BROWN ("brown",  "marron",   BossBar.Color.WHITE,  0x8B5A2B),
    UNKNOWN("unknown", "desconocido", BossBar.Color.WHITE, 0x808080);

    private final String englishName;
    private final String commandName;
    private final BossBar.Color bossBarColor;
    private final int rgb;

    GameColor(String englishName, String commandName, BossBar.Color bossBarColor, int rgb) {
        this.englishName = englishName;
        this.commandName = commandName;
        this.bossBarColor = bossBarColor;
        this.rgb = rgb;
    }

    public String englishName() {
        return englishName;
    }

    public String commandName() {
        return commandName;
    }

    public BossBar.Color bossBarColor() {
        return bossBarColor;
    }

    public int rgb() {
        return rgb;
    }

    public static GameColor fromCommand(String value) {
        for (GameColor color : values()) {
            if (color.commandName.equalsIgnoreCase(value) || color.englishName.equalsIgnoreCase(value)) {
                return color;
            }
        }
        return UNKNOWN;
    }
}
