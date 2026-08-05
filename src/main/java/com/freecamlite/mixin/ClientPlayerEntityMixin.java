package com.freecamlite.mixin;

import com.freecamlite.FreecamManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * tickMovement() is where WASD/jump/gravity/travel get processed for the local
 * player each tick. Cancelling it entirely while freecam is active means the
 * player never moves and never sends a changed position to the server - no
 * rubber-banding, no desync, just a frozen body while the camera flies off.
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

	@Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
	private void freecamlite$freezeWhileActive(CallbackInfo ci) {
		if (FreecamManager.isActive()) {
			ci.cancel();
		}
	}
}
