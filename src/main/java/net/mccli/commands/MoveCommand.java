package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MoveCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (jsonObject.has("forward")) mc.options.keyUp.setDown(jsonObject.get("forward").getAsBoolean());
                if (jsonObject.has("back")) mc.options.keyDown.setDown(jsonObject.get("back").getAsBoolean());
                if (jsonObject.has("left")) mc.options.keyLeft.setDown(jsonObject.get("left").getAsBoolean());
                if (jsonObject.has("right")) mc.options.keyRight.setDown(jsonObject.get("right").getAsBoolean());
                if (jsonObject.has("jump")) mc.options.keyJump.setDown(jsonObject.get("jump").getAsBoolean());
                if (jsonObject.has("sneak")) mc.options.keyShift.setDown(jsonObject.get("sneak").getAsBoolean());
                if (jsonObject.has("sprint")) mc.options.keySprint.setDown(jsonObject.get("sprint").getAsBoolean());

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error processing movement: " + e.getMessage()));
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
