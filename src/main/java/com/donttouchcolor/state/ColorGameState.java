package com.donttouchcolor.state;

import com.donttouchcolor.color.GameColor;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ColorGameState {
    public enum Mode { OFF, FIXED, RANDOM }

    private final ServerBossBar bossBar = new ServerBossBar(Text.literal("DontTouchColor"), BossBar.Color.WHITE, BossBar.Style.PROGRESS);
    private GameColor activeColor = GameColor.BLUE;
    private Mode mode = Mode.OFF;
    private long nextRollTick = 0;
    private int randomTimerTicks = 20 * 60;

    public void setFixed(GameColor color) {
        this.mode = Mode.FIXED;
        this.activeColor = color;
        refreshBossBar();
    }

    public void startRandom(int timerSeconds, long currentTick) {
        this.mode = Mode.RANDOM;
        this.randomTimerTicks = Math.max(20 * 5, timerSeconds * 20);
        randomizeColor();
        this.nextRollTick = currentTick + randomTimerTicks;
        refreshBossBar();
    }

    public void stop() {
        mode = Mode.OFF;
        bossBar.clearPlayers();
    }

    public void tick(MinecraftServer server) {
        if (mode == Mode.OFF) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            bossBar.addPlayer(player);
        }
        if (mode == Mode.RANDOM && server.getOverworld().getTime() >= nextRollTick) {
            randomizeColor();
            nextRollTick = server.getOverworld().getTime() + randomTimerTicks;
            refreshBossBar();
        }
    }

    private void randomizeColor() {
        GameColor[] colors = {GameColor.YELLOW, GameColor.BLUE, GameColor.RED, GameColor.GREEN, GameColor.BROWN};
        activeColor = colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    private void refreshBossBar() {
        bossBar.setName(Text.literal("Color actual: " + activeColor.commandName()));
        bossBar.setColor(activeColor.bossBarColor());
        bossBar.setPercent(1.0F);
    }

    public GameColor getActiveColor() {
        return activeColor;
    }

    public Mode getMode() {
        return mode;
    }

    public void setRandomTimerSeconds(int seconds) {
        randomTimerTicks = Math.max(20 * 5, seconds * 20);
    }
}
