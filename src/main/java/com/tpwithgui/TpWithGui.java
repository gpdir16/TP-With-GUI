package com.tpwithgui;

import com.tpwithgui.screen.TeleportScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TpWithGui implements ClientModInitializer {
	public static final String MOD_ID = "tpwithgui";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing TP With GUI...");

		openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.tpwithgui.open_gui",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_K,
				KeyMapping.Category.MISC));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("tpgui")
						.executes(context -> {
							Minecraft client = context.getSource().getClient();
							client.execute(() -> client.gui.setScreen(new TeleportScreen()));
							return 1;
						})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal(".")
						.executes(context -> {
							Minecraft client = context.getSource().getClient();
							client.execute(() -> client.gui.setScreen(new TeleportScreen()));
							return 1;
						})));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.consumeClick()) {
				if (client.gui.screen() == null) {
					client.execute(() -> client.gui.setScreen(new TeleportScreen()));
				}
			}
		});

		LOGGER.info("TP With GUI initialized successfully");
	}
}
