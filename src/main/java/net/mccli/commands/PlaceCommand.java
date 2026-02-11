package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PlaceCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return error("Player/GameMode not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (!jsonObject.has("x") || !jsonObject.has("y") || !jsonObject.has("z") || !jsonObject.has("face")) {
                    future.complete(error("Missing arguments"));
                    return;
                }
                int x = jsonObject.get("x").getAsInt();
                int y = jsonObject.get("y").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                String face = jsonObject.get("face").getAsString().toUpperCase();
                Direction direction = Direction.valueOf(face);

                BlockPos pos = new BlockPos(x, y, z);
                Vec3 vec = new Vec3(x + 0.5, y + 0.5, z + 0.5); // Center of block
                BlockHitResult hit = new BlockHitResult(vec, direction, pos, false);

                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                mc.player.swing(InteractionHand.MAIN_HAND);

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error placing: " + e.getMessage()));
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
