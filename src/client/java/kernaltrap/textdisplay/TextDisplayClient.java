// TextDisplayClient.java - Main class
// TextDisplay-MC Copyright 2024 (c) kernaltrap8
// Licensed under BSD-3

package kernaltrap.textdisplay;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import java.awt.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class TextDisplayClient implements ClientModInitializer {
	// Variables
	public static TextDisplayClient INSTANCE;
	public int useRgbText = 0;
	public int useCustomRgb = 0;
	public int TextDisplayColor = 0xFFFFFF;
	public int padding = 10;
	public boolean useShadow = true;
	public String text = "";
	public String TextDisplayLogPrefix = "[§dTD§f]";
	public String TextDisplayLogError = "§c[§dTD§f§c]";
	//public String TextDisplayLogResetColor = "§f";
	public String commandPostfix = "td";
	public String commandSetText = "text";
	public String messageSetText = TextDisplayLogPrefix + " " + "Set display text to: ";
	public String commandRgb = "rgb";
	public String messageRgbEnabled = TextDisplayLogPrefix + " " + "Enabled rainbow text!";
	public String messageRgbDisabled = TextDisplayLogPrefix + " " + "Disabled rainbow text!";
	public String commandColor = "color";
	public String messageColor = TextDisplayLogPrefix + " " + "Set custom color to #";
	public String messageColorInvalid = TextDisplayLogError + " " + "Invalid hex color!";
	public String textIsEmptyMessage = TextDisplayLogError + " " + "Text is empty!";
	public String commandPadding = "padding";
	public String messagePadding = TextDisplayLogPrefix + " " + "Set padding to ";
	public String commandShadow = "shadow";
	public String messageShadowOn = TextDisplayLogPrefix + " " + "Enabled text shadow!";
	public String messageShadowOff = TextDisplayLogPrefix + " " + "Disabled text shadow!";
	public String commandReset = "reset";
	public String messageReset = TextDisplayLogPrefix + " " + "Reset all settings to default!";
	public String commandPosition = "position";
	public String messagePosition = TextDisplayLogPrefix + " " + "Text position set to: ";
	public String textPosition = "bottom_right"; // Default position
	private boolean checkTextEmpty(CommandContext<FabricClientCommandSource> context) {
		if (INSTANCE.text.isEmpty()) {
			context.getSource().sendError(Text.literal(textIsEmptyMessage));
			return true;
		}
		return false;
	}
	@Override
	public void onInitializeClient() {
		INSTANCE = this;

		// Register HUD renderer
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null) return;

			int screenWidth = context.getScaledWindowWidth();
			int screenHeight = context.getScaledWindowHeight();
			int textHeight = client.textRenderer.fontHeight;
			int textWidth = client.textRenderer.getWidth(text);

			// Determine x and y based on position
			int x = 0;
			int y = switch (textPosition) {
                case "top_left" -> {
                    x = padding;
                    yield padding;
                }
                case "top_right" -> {
                    x = screenWidth - textWidth - padding;
                    yield padding;
                }
                case "bottom_left" -> {
                    x = padding;
                    yield screenHeight - textHeight - padding;
                }
                case "bottom_right" -> {
                    x = screenWidth - textWidth - padding;
                    yield screenHeight - textHeight - padding;
                }
                case "top_center" -> {
                    x = (screenWidth - textWidth) / 2;
                    yield padding;
                }
                case "bottom_center" -> {
                    x = (screenWidth - textWidth) / 2;
                    yield screenHeight - textHeight - padding;
                }
                default -> 0;

                // Set position based on the selected text position
            };

            if (useRgbText == 1) {
				useShadow = false;
				// Apply an RGB effect per character
				long rgbTime = System.currentTimeMillis() / 10; // Adjust speed
				int charXPosition = x;
				for (int i = 0; i < text.length(); i++) {
					char currentChar = text.charAt(i);
					float hue = ((rgbTime + (i * 20)) % 360) / 360.0f;
					int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);

					Text charText = Text.literal(String.valueOf(currentChar))
							.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));

					context.drawText(client.textRenderer, charText, charXPosition, y, 0xFFFFFF, useShadow);
					charXPosition += client.textRenderer.getWidth(String.valueOf(currentChar)); // Move to next character position
				}
			}
			if (useCustomRgb == 1) {
				context.drawText(client.textRenderer, text, x, y, TextDisplayColor, useShadow);
			}
			if (useCustomRgb == 0 && useRgbText == 0) {
				context.drawText(client.textRenderer, text, x, y, 0xFFFFFF, useShadow);
			}
		});

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register(this::register);
	}

	private void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(commandPostfix)
				.then(literal(commandSetText)
						.then(argument("text", StringArgumentType.greedyString())
								.executes(context -> {
									String newText = StringArgumentType.getString(context, "text");
									INSTANCE.text = newText;
									context.getSource().sendFeedback(Text.literal(messageSetText + newText));
									return 1;
								}))
				)
				.then(literal(commandRgb)
						.then(literal("on").executes(context -> {
							INSTANCE.useRgbText = 1;
							INSTANCE.useCustomRgb = 0;
							context.getSource().sendFeedback(Text.literal(messageRgbEnabled));
							return 1;
						}))
						.then(literal("off").executes(context -> {
							INSTANCE.useRgbText = 0;
							context.getSource().sendFeedback(Text.literal(messageRgbDisabled));
							return 1;
						}))
				)
				.then(literal(commandColor)
						.then(argument("hex", StringArgumentType.string())
								.executes(context -> {
									boolean result = checkTextEmpty(context);
									if (result) return 0;
									String hex = StringArgumentType.getString(context, "hex");
									try {
										hex = hex.replace("#", "");
										INSTANCE.TextDisplayColor = Integer.parseInt(hex, 16);
										INSTANCE.useCustomRgb = 1;
										INSTANCE.useRgbText = 0;
										context.getSource().sendFeedback(Text.literal(messageColor + hex));
									} catch (NumberFormatException e) {
										context.getSource().sendError(Text.literal(messageColorInvalid));
									}
									return 1;
								}))
				)
				.then(literal(commandPadding)
						.then(argument("pixels", IntegerArgumentType.integer(0))
								.executes(context -> {
									boolean result = checkTextEmpty(context);
									if (result) return 0;
									INSTANCE.padding = IntegerArgumentType.getInteger(context, "pixels");
									context.getSource().sendFeedback(Text.literal(messagePadding + padding + " " + "pixels!"));
									return 1;
								}))
				)
				.then(literal(commandShadow)
						.then(literal("on").executes(context -> {
							boolean result = checkTextEmpty(context);
							if (result) return 0;
							INSTANCE.useShadow = true;
							context.getSource().sendFeedback(Text.literal(messageShadowOn));
							return 1;
						}))
						.then(literal("off").executes(context -> {
							boolean result = checkTextEmpty(context);
							if (result) return 0;
							INSTANCE.useShadow = false;
							context.getSource().sendFeedback(Text.literal(messageShadowOff));
							return 1;
						}))
				)
				.then(literal(commandReset).executes(context -> {
					INSTANCE.useRgbText = 0;
					INSTANCE.useCustomRgb = 0;
					INSTANCE.TextDisplayColor = 0xFFFFFF;
					INSTANCE.padding = 10;
					INSTANCE.text = "";
					INSTANCE.useShadow = true;
					INSTANCE.textPosition = "bottom_right";
					context.getSource().sendFeedback(Text.literal(messageReset));
					return 1;
				}))
				.then(literal(commandPosition)
						.then(argument("position", StringArgumentType.word())
								.suggests((context, builder) -> CommandSource.suggestMatching(
										new String[]{
												"top_left", "top_right", "bottom_left", "bottom_right",
												"top_center", "bottom_center"
										}, builder
								))
								.executes(context -> {
									boolean result = checkTextEmpty(context);
									if (result) return 0;
									String position = StringArgumentType.getString(context, "position");
									if (position.equals("top_left") || position.equals("top_right") || position.equals("bottom_left")
											|| position.equals("bottom_right") || position.equals("top_center") || position.equals("bottom_center")) {
										INSTANCE.textPosition = position;
										context.getSource().sendFeedback(Text.literal(messagePosition + position));
									} else {
										context.getSource().sendError(Text.literal("Invalid position! Valid options: top_left, top_right, bottom_left, bottom_right, top_center, bottom_center"));
									}
									return 1;
								}))
				)
		);
	}
}
