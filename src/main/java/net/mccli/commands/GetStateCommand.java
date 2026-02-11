package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GetStateCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found (not in game?)");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                Player player = mc.player;
                if (player == null) {
                     future.complete(error("Player not found"));
                     return;
                }

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");

                JsonObject data = new JsonObject();
                data.addProperty("name", player.getName().getString());
                data.addProperty("health", player.getHealth());
                data.addProperty("maxHealth", player.getMaxHealth());
                data.addProperty("foodLevel", player.getFoodData().getFoodLevel());
                data.addProperty("saturation", player.getFoodData().getSaturationLevel());
                data.addProperty("x", player.getX());
                data.addProperty("y", player.getY());
                data.addProperty("z", player.getZ());
                data.addProperty("yaw", player.getYRot());
                data.addProperty("pitch", player.getXRot());
                data.addProperty("dimension", player.level().dimension().location().toString());
                if (mc.gameMode != null) {
                    data.addProperty("gamemode", mc.gameMode.getPlayerMode().getName());
                }
                data.addProperty("isAlive", player.isAlive());
                data.addProperty("isOnGround", player.onGround());

                response.add("data", data);
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error getting state: " + e.getMessage()));
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
