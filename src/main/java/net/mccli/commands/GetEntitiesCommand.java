package net.mccli.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GetEntitiesCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return error("Level/Player not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                double radius = jsonObject.has("radius") ? jsonObject.get("radius").getAsDouble() : 50.0;
                String typeFilter = jsonObject.has("type") ? jsonObject.get("type").getAsString() : null;

                AABB area = mc.player.getBoundingBox().inflate(radius);
                List<Entity> entities = mc.level.getEntitiesOfClass(Entity.class, area);

                JsonArray array = new JsonArray();
                for (Entity e : entities) {
                    String id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
                    if (typeFilter != null && !id.equals(typeFilter)) continue;

                    JsonObject entity = new JsonObject();
                    entity.addProperty("id", id);
                    entity.addProperty("uuid", e.getUUID().toString());
                    entity.addProperty("x", e.getX());
                    entity.addProperty("y", e.getY());
                    entity.addProperty("z", e.getZ());
                    if (e instanceof LivingEntity) {
                        entity.addProperty("health", ((LivingEntity)e).getHealth());
                        entity.addProperty("maxHealth", ((LivingEntity)e).getMaxHealth());
                    }
                    array.add(entity);
                }

                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                response.add("data", array);
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error getting entities: " + e.getMessage()));
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
