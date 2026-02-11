package net.mccli.commands;

import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class InspectBlockCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return error("Level not found");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                if (!jsonObject.has("x") || !jsonObject.has("y") || !jsonObject.has("z")) {
                    future.complete(error("Missing coordinates"));
                    return;
                }
                int x = jsonObject.get("x").getAsInt();
                int y = jsonObject.get("y").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                BlockPos pos = new BlockPos(x, y, z);

                BlockState state = mc.level.getBlockState(pos);
                JsonObject response = new JsonObject();
                response.addProperty("status", "success");

                JsonObject data = new JsonObject();
                data.addProperty("id", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());

                JsonObject properties = new JsonObject();
                state.getValues().forEach((property, value) -> {
                    properties.addProperty(property.getName(), value.toString());
                });
                data.add("properties", properties);

                BlockEntity be = mc.level.getBlockEntity(pos);
                if (be != null) {
                    try {
                         data.addProperty("nbt", be.saveWithFullMetadata(mc.level.registryAccess()).toString());
                    } catch (Exception e) {
                         // ignore NBT errors
                    }
                }

                response.add("data", data);
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error inspecting block: " + e.getMessage()));
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
