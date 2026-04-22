package com.donttouchcolor.command;

import com.donttouchcolor.DontTouchColorMod;
import com.donttouchcolor.color.GameColor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class DontTouchColorCommand {
    private DontTouchColorCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("donttouchcolor")
            .then(CommandManager.literal("stop")
                .executes(ctx -> {
                    DontTouchColorMod.STATE.stop();
                    ctx.getSource().sendFeedback(() -> Text.literal("DontTouchColor desactivado."), false);
                    return 1;
                }))
            .then(CommandManager.literal("random")
                .then(CommandManager.literal("start")
                    .executes(ctx -> {
                        DontTouchColorMod.STATE.startRandom(
                            DontTouchColorMod.CONFIG.getRandomTimerSeconds(),
                            ctx.getSource().getServer().getOverworld().getTime());
                        ctx.getSource().sendFeedback(() -> Text.literal("Modo random iniciado."), false);
                        return 1;
                    })))
            .then(CommandManager.literal("options")
                .then(CommandManager.literal("randomtimer")
                    .then(CommandManager.argument("seconds", StringArgumentType.word())
                        .executes(ctx -> {
                            String raw = StringArgumentType.getString(ctx, "seconds");
                            int seconds = parseSeconds(raw);
                            DontTouchColorMod.CONFIG.setRandomTimerSeconds(seconds);
                            DontTouchColorMod.CONFIG.save();
                            DontTouchColorMod.STATE.setRandomTimerSeconds(seconds);
                            ctx.getSource().sendFeedback(() -> Text.literal("randomtimer actualizado a " + seconds + "s"), false);
                            return 1;
                        }))))
            .then(CommandManager.literal("gloverecipe")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    DontTouchColorMod.GLOVE_RECIPE_EDITOR.open(player, DontTouchColorMod.CONFIG);
                    return 1;
                })
                .then(CommandManager.literal("update")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        if (DontTouchColorMod.GLOVE_RECIPE_EDITOR.update(player, DontTouchColorMod.CONFIG)) {
                            ctx.getSource().sendFeedback(() -> Text.literal("Receta del guante actualizada."), false);
                            return 1;
                        }
                        ctx.getSource().sendError(Text.literal("Abre /donttouchcolor gloverecipe primero."));
                        return 0;
                    })))
            .then(CommandManager.literal("giveglove")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    player.giveItemStack(DontTouchColorMod.PROTECTIVE_GLOVE.getDefaultStack());
                    return 1;
                }))
            .then(CommandManager.argument("color", StringArgumentType.word())
                .then(CommandManager.literal("start")
                    .executes(ctx -> {
                        String colorArg = StringArgumentType.getString(ctx, "color");
                        GameColor color = GameColor.fromCommand(colorArg);
                        if (color == GameColor.UNKNOWN) {
                            ctx.getSource().sendError(Text.literal("Color invalido. Usa amarillo, azul, rojo, verde, marron."));
                            return 0;
                        }
                        DontTouchColorMod.STATE.setFixed(color);
                        ctx.getSource().sendFeedback(() -> Text.literal("Color activo: " + color.commandName()), false);
                        return 1;
                    }))));
    }

    private static int parseSeconds(String raw) {
        String normalized = raw.trim().toLowerCase();
        if (normalized.endsWith("s")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        try {
            return Math.max(5, Integer.parseInt(normalized));
        } catch (NumberFormatException e) {
            return 60;
        }
    }
}
