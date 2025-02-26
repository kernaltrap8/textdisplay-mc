package kernaltrap.textdisplay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;

public class TextDisplayClient implements ClientModInitializer {
	public static final String MOD_ID = "fabric-docs-reference";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null) LOGGER.info("Player and/or client is null!");

			String TextDisplayText = "Hello from TextDisplayClient!";
			int TextDisplayColor = 0xFFFFFF;
			int TextDisplayPadding = 10;

			int screenWidth = context.getScaledWindowWidth();
			int screenHeight = context.getScaledWindowHeight();
			int textWidth = client.textRenderer.getWidth(TextDisplayText);
			int textHeight = client.textRenderer.fontHeight;

            int x = screenWidth - textWidth - TextDisplayPadding;
            int y = screenHeight - textHeight - TextDisplayPadding;

            context.drawText(client.textRenderer, Text.literal(TextDisplayText), x, y, TextDisplayColor, true);
		});
	}
}
