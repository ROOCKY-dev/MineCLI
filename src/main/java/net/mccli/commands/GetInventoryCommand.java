package net.mccli.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GetInventoryCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return error("Player not found");
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

                JsonObject inventory = new JsonObject();

                JsonArray main = new JsonArray();
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    main.add(serializeItemStack(player.getInventory().items.get(i), i));
                }
                inventory.add("main", main);

                JsonArray armor = new JsonArray();
                for (int i = 0; i < player.getInventory().armor.size(); i++) {
                    armor.add(serializeItemStack(player.getInventory().armor.get(i), i));
                }
                inventory.add("armor", armor);

                JsonArray offhand = new JsonArray();
                for (int i = 0; i < player.getInventory().offhand.size(); i++) {
                    offhand.add(serializeItemStack(player.getInventory().offhand.get(i), i));
                }
                inventory.add("offhand", offhand);

                inventory.addProperty("selectedSlot", player.getInventory().selected);

                response.add("data", inventory);
                future.complete(CommandHandler.GSON.toJson(response));
            } catch (Exception e) {
                future.complete(error("Error getting inventory: " + e.getMessage()));
            }
        });

        try {
            return future.get();
        } catch (Exception e) {
            return error("Error waiting for main thread: " + e.getMessage());
        }
    }

    private JsonObject serializeItemStack(ItemStack stack, int slot) {
        JsonObject item = new JsonObject();
        item.addProperty("slot", slot);
        if (stack.isEmpty()) {
            item.addProperty("id", "minecraft:air");
            item.addProperty("count", 0);
        } else {
            item.addProperty("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            item.addProperty("count", stack.getCount());
            if (stack.isDamageableItem()) {
                item.addProperty("damage", stack.getDamageValue());
                item.addProperty("maxDamage", stack.getMaxDamage());
            }
            // Simple check for custom name
            item.addProperty("displayName", stack.getHoverName().getString());
        }
        return item;
    }

    private String error(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", message);
        return CommandHandler.GSON.toJson(json);
    }
}
