package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EquipCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (!jsonObject.has("slot")) {
                    future.complete(error("Missing 'slot'"));
                    return;
                }
                int slot = jsonObject.get("slot").getAsInt();
                if (slot >= 0 && slot < 9) {
                    mc.player.getInventory().selected = slot;
                    JsonObject response = new JsonObject();
                    response.addProperty("status", "success");
                    future.complete(CommandHandler.GSON.toJson(response));
                } else {
                    future.complete(error("Invalid slot (0-8)"));
                }
            } catch (Exception e) {
                future.complete(error("Error equipping: " + e.getMessage()));
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
