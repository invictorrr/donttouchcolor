package com.donttouchcolor.rules;

import com.donttouchcolor.DontTouchColorMod;
import com.donttouchcolor.color.ColorClassifier;
import com.donttouchcolor.color.GameColor;
import com.donttouchcolor.state.ColorGameState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class DeathRules {
    private final ColorGameState state;
    private final ColorClassifier classifier;

    public DeathRules(ColorGameState state, ColorClassifier classifier) {
        this.state = state;
        this.classifier = classifier;
    }

    public void checkPlayer(ServerPlayerEntity player) {
        if (state.getMode() == ColorGameState.Mode.OFF || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }
        GameColor active = state.getActiveColor();

        // 1. Inventory check (glove provides protection)
        if (!hasGloveProtection(player) && hasDangerousInventory(player, active)) {
            kill(player, "Tocaste color " + active.commandName() + " en tu inventario.");
            return;
        }

        // 2. Physical block contact (no glove protection — you can't wear a glove on a block)
        if (isTouchingColoredBlock(player, active)) {
            kill(player, "Pisaste/tocaste un bloque " + active.commandName() + ".");
            return;
        }

        // 3. Animal proximity (bounding-box collision)
        for (Entity entity : player.getEntityWorld().getOtherEntities(player,
                player.getBoundingBox().expand(0.1D), e -> e instanceof AnimalEntity)) {
            if (classifier.classifyEntity(entity) == active) {
                kill(player, "Colisionaste con animal color " + active.commandName() + ".");
                return;
            }
        }
    }

    public void onDirectAnimalTouch(ServerPlayerEntity player, Entity entity) {
        if (state.getMode() == ColorGameState.Mode.OFF || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!(entity instanceof AnimalEntity)) {
            return;
        }
        if (classifier.classifyEntity(entity) == state.getActiveColor()) {
            kill(player, "Tocaste un animal color " + state.getActiveColor().commandName() + ".");
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Returns true if the player's bounding box (expanded by 0.05 to catch
     * the block they're standing on) touches any block of {@code activeColor}.
     */
    private boolean isTouchingColoredBlock(ServerPlayerEntity player, GameColor activeColor) {
        Box box = player.getBoundingBox().expand(0.05);
        BlockPos min = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            BlockState state = player.getEntityWorld().getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (classifier.classifyBlock(state.getBlock()) == activeColor) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDangerousInventory(ServerPlayerEntity player, GameColor activeColor) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && classifier.classifyItem(stack.getItem()) == activeColor) {
                return true;
            }
        }
        return false;
    }

    private boolean hasGloveProtection(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isOf(DontTouchColorMod.PROTECTIVE_GLOVE)) {
                return true;
            }
        }
        return false;
    }

    private void kill(ServerPlayerEntity player, String reason) {
        player.sendMessage(Text.literal(reason), false);
        player.setHealth(0.0F);
    }
}
