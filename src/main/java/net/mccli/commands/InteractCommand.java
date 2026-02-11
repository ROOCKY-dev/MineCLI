package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class InteractCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return error("Player or GameMode not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (!jsonObject.has("action")) {
                    future.complete(error("Missing 'action'"));
                    return;
                }

                String action = jsonObject.get("action").getAsString();
                if ("attack".equals(action)) {
                     if (mc.hitResult != null) {
                         if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
                             mc.gameMode.attack(mc.player, ((EntityHitResult)mc.hitResult).getEntity());
                         } else if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                             BlockHitResult blockHit = (BlockHitResult)mc.hitResult;
                             mc.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
                         }
                     }
                     mc.player.swing(InteractionHand.MAIN_HAND);
                } else if ("use".equals(action)) {
                     InteractionHand hand = InteractionHand.MAIN_HAND;

                     if (!mc.player.isUsingItem()) {
                         if (mc.hitResult != null) {
                             if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
                                 mc.gameMode.interact(mc.player, ((EntityHitResult)mc.hitResult).getEntity(), hand);
                             } else if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                                 mc.gameMode.useItemOn(mc.player, hand, (BlockHitResult)mc.hitResult);
                             }
                         }
                         mc.gameMode.useItem(mc.player, hand);
                     }
                } else if ("stop_use".equals(action)) {
                     mc.gameMode.releaseUsingItem(mc.player);
                } else if ("drop".equals(action)) {
                    mc.player.drop(false);
                } else if ("drop_stack".equals(action)) {
                    mc.player.drop(true);
                }

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error processing interact: " + e.getMessage()));
            }
        });

        try {
            return future.get();
        } catch (Exception e) {
            return error("Error waiting for main thread: " + e.getMessage());
        }
    }

    private String error(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", message);
        return CommandHandler.GSON.toJson(json);
    }
}
