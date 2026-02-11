package net.mccli.events;

import net.mccli.McCliMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = McCliMod.MODID, value = Dist.CLIENT)
public class MovementManager {
    public static Vec3 target = null;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || target == null) return;

        Vec3 playerPos = mc.player.position();
        // Check horizontal distance mainly
        double dx = target.x - playerPos.x;
        double dz = target.z - playerPos.z;
        double distSq = dx * dx + dz * dz;

        if (distSq < 0.5) { // Stop if within 0.7 blocks approx
            target = null;
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
            return;
        }

        // Look at target
        float yaw = (float) (Math.atan2(dz, dx) * (180 / Math.PI)) - 90;
        mc.player.setYRot(yaw);
        // Maybe interpolate or smooth? Nah, MVP.

        // Move forward
        mc.options.keyUp.setDown(true);

        // Auto jump if blocked
        if (mc.player.horizontalCollision && mc.player.onGround()) {
             mc.options.keyJump.setDown(true);
        } else {
             mc.options.keyJump.setDown(false);
        }
    }
}
