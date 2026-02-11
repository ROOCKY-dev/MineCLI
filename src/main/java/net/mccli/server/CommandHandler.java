package net.mccli.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.mccli.McCliMod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandHandler {
    public static final Gson GSON = new Gson();
    private static final Map<String, Function<JsonObject, String>> COMMANDS = new HashMap<>();

    static {
        // Register default commands
        COMMANDS.put("ping", (json) -> {
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "pong");
            return GSON.toJson(response);
        });
        COMMANDS.put("state", new net.mccli.commands.GetStateCommand());
        COMMANDS.put("inventory", new net.mccli.commands.GetInventoryCommand());
        COMMANDS.put("move", new net.mccli.commands.MoveCommand());
        COMMANDS.put("look", new net.mccli.commands.LookCommand());
        COMMANDS.put("interact", new net.mccli.commands.InteractCommand());
        COMMANDS.put("view", new net.mccli.commands.GetViewCommand());
        COMMANDS.put("ui", new net.mccli.commands.GetUICommand());
        COMMANDS.put("click_slot", new net.mccli.commands.InventoryClickCommand());
    }

    public static String handle(String input) {
        try {
            JsonObject json = GSON.fromJson(input, JsonObject.class);
            if (!json.has("command")) {
                return error("Missing 'command' field");
            }
            String commandName = json.get("command").getAsString();
            if (COMMANDS.containsKey(commandName)) {
                try {
                    return COMMANDS.get(commandName).apply(json);
                } catch (Exception e) {
                    McCliMod.LOGGER.error("Error executing command " + commandName, e);
                    return error("Error executing command: " + e.getMessage());
                }
            } else {
                return error("Unknown command: " + commandName);
            }
        } catch (JsonSyntaxException e) {
            return error("Invalid JSON");
        }
    }

    private static String error(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", message);
        return GSON.toJson(json);
    }

    public static void register(String name, Function<JsonObject, String> handler) {
        COMMANDS.put(name, handler);
    }
}
