package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.events.MovementManager;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GotoCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (!jsonObject.has("x") || !jsonObject.has("z")) { // y is optional maybe?
                    future.complete(error("Missing coordinates"));
                    return;
                }
                double x = jsonObject.get("x").getAsDouble();
                double y = jsonObject.has("y") ? jsonObject.get("y").getAsDouble() : mc.player.getY();
                double z = jsonObject.get("z").getAsDouble();

                MovementManager.target = new Vec3(x, y, z);

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error setting target: " + e.getMessage()));
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
