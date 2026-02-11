package net.mccli.commands;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mccli.server.CommandHandler;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GetViewCommand implements Function<JsonObject, String> {
    @Override
    public String apply(JsonObject jsonObject) {
        Minecraft mc = Minecraft.getInstance();

        CompletableFuture<String> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                RenderTarget target = mc.getMainRenderTarget();
                try (NativeImage img = new NativeImage(target.width, target.height, false)) {
                     RenderSystem.bindTexture(target.getColorTextureId());
                     img.downloadTexture(0, true);
                     img.flipY();

                     File tempFile = File.createTempFile("screenshot", ".png");
                     img.writeToFile(tempFile);

                     byte[] bytes = Files.readAllBytes(tempFile.toPath());
                     tempFile.delete();

                     String base64 = Base64.getEncoder().encodeToString(bytes);
                     JsonObject response = new JsonObject();
                     response.addProperty("status", "success");
                     response.addProperty("image", base64);
                     response.addProperty("width", img.getWidth());
                     response.addProperty("height", img.getHeight());
                     future.complete(CommandHandler.GSON.toJson(response));
                }

            } catch (Exception e) {
                future.complete(error("Error taking screenshot: " + e.getMessage()));
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
