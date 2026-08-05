package com.freecamlite;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Entry point. Registers a single keybind (default: R, rebindable in the
 * vanilla Options > Controls screen - this mod has no config screen of its own)
 * and drives FreecamManager once per client tick.
 */
public class FreecamClient implements ClientModInitializer {

	public static final String MOD_ID = "freecamlite";

	private static KeyBinding toggleKey;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.freecamlite.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category.freecamlite"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				FreecamManager.toggle();
			}
			FreecamManager.onClientTick(client);
		});

		// Safety net: don't stay "stuck" in freecam state across a disconnect/reconnect.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FreecamManager.forceDisable());
	}
}
