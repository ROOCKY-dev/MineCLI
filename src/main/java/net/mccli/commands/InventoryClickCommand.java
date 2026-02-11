package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class InventoryClickCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (!jsonObject.has("containerId") || !jsonObject.has("slot")) {
                    future.complete(error("Missing 'containerId' or 'slot'"));
                    return;
                }

                int containerId = jsonObject.get("containerId").getAsInt();
                int slot = jsonObject.get("slot").getAsInt();
                int button = jsonObject.has("button") ? jsonObject.get("button").getAsInt() : 0;

                ClickType type = ClickType.PICKUP;
                if (jsonObject.has("type")) {
                    try {
                        type = ClickType.valueOf(jsonObject.get("type").getAsString().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        future.complete(error("Invalid click type"));
                        return;
                    }
                }

                mc.gameMode.handleInventoryMouseClick(containerId, slot, button, type, mc.player);

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error clicking inventory: " + e.getMessage()));
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
