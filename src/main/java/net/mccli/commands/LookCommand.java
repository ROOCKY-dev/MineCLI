package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class LookCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (jsonObject.has("yaw")) mc.player.setYRot(jsonObject.get("yaw").getAsFloat());
                if (jsonObject.has("pitch")) mc.player.setXRot(jsonObject.get("pitch").getAsFloat());

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error processing look: " + e.getMessage()));
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
