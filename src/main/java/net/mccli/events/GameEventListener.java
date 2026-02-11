package net.mccli.events;

import com.google.gson.JsonObject;
import net.mccli.McCliMod;
import net.mccli.server.ApiServer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = McCliMod.MODID, value = Dist.CLIENT)
public class GameEventListener {

    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        JsonObject json = new JsonObject();
        json.addProperty("event", "chat");
        json.addProperty("message", event.getMessage().getString());
        ApiServer.broadcast(json);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            JsonObject json = new JsonObject();
            json.addProperty("event", "death");
            json.addProperty("message", "Player died");
            ApiServer.broadcast(json);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            JsonObject json = new JsonObject();
            json.addProperty("event", "join");
            json.addProperty("dimension", event.getLevel().dimension().location().toString());
            ApiServer.broadcast(json);
        }
    }
}
