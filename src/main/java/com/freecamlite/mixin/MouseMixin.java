package com.freecamlite.mixin;

import com.freecamlite.FreecamManager;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mouse#updateMouse() normally ends by calling player.changeLookDirection(dx, dy)
 * with the already sensitivity-scaled deltas. While freecam is active we steal
 * that call and rotate the detached camera instead, so the player's own facing
 * never changes.
 */
@Mixin(Mouse.class)
public class MouseMixin {

	@Redirect(
			method = "updateMouse",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
			)
	)
	private void freecamlite$redirectLook(ClientPlayerEntity player, double dx, double dy) {
		if (FreecamManager.isActive()) {
			FreecamManager.rotate(dx, dy);
		} else {
			player.changeLookDirection(dx, dy);
		}
	}
}
