package net.mccli.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GetUICommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                Screen screen = mc.screen;
                JsonObject response = new JsonObject();
                response.addProperty("status", "success");

                if (screen == null) {
                    response.addProperty("type", "none");
                } else {
                    response.addProperty("type", screen.getClass().getName());
                    response.addProperty("title", screen.getTitle().getString());

                    if (screen instanceof AbstractContainerScreen) {
                        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
                        response.addProperty("containerId", containerScreen.getMenu().containerId);
                        JsonArray slots = new JsonArray();
                        for (Slot slot : containerScreen.getMenu().slots) {
                             slots.add(serializeSlot(slot));
                        }
                        response.add("slots", slots);
                    }
                }

                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error getting UI: " + e.getMessage()));
            }
        });

        try {
            return future.get();
        } catch (Exception e) {
            return error("Error waiting for main thread: " + e.getMessage());
        }
    }

    private JsonObject serializeSlot(Slot slot) {
        JsonObject json = new JsonObject();
        json.addProperty("index", slot.index);
        // Usually slot.index is the index in the container.
        // There is also slot.getSlotIndex() which is index in inventory.
        json.addProperty("slotIndex", slot.getSlotIndex());
        json.addProperty("container", slot.container.getClass().getName()); // identify if player inventory or chest

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) {
            json.addProperty("id", "minecraft:air");
            json.addProperty("count", 0);
        } else {
            json.addProperty("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            json.addProperty("count", stack.getCount());
            if (stack.isDamageableItem()) {
                json.addProperty("damage", stack.getDamageValue());
                json.addProperty("maxDamage", stack.getMaxDamage());
            }
             json.addProperty("displayName", stack.getHoverName().getString());
        }
        return json;
    }

    private String error(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", message);
        return CommandHandler.GSON.toJson(json);
    }
}
