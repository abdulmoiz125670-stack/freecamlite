package com.freecamlite;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.MarkerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Holds all freecam state and does all the work:
 *  - creates/destroys a detached "camera" entity (a vanilla MarkerEntity -
 *    invisible, no collision, never ticked by the world - we drive it by hand)
 *  - swaps MinecraftClient#cameraEntity to it and back
 *  - moves it from raw WASD/Space/Shift/Sprint key state each client tick
 *  - rotates it from mouse deltas (see MouseMixin, which redirects look input
 *    here instead of to the player while active)
 *  - the player itself is frozen by ClientPlayerEntityMixin, which cancels
 *    tickMovement() while freecam is active
 */
public final class FreecamManager {

	// Tune these to taste - no config screen, just edit and rebuild.
	private static final double BASE_SPEED = 0.5D;       // blocks per tick
	private static final double SPRINT_MULTIPLIER = 3.0D; // hold Sprint key to go this much faster
	private static final float MOUSE_SENSITIVITY = 0.15F;

	private static boolean active = false;
	private static MarkerEntity camera;

	private FreecamManager() {
	}

	public static boolean isActive() {
		return active;
	}

	public static void toggle() {
		if (active) {
			disable();
		} else {
			enable();
		}
	}

	/** Force off with no side effects, e.g. on disconnect. Safe to call at any time. */
	public static void forceDisable() {
		active = false;
		camera = null;
	}

	private static void enable() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) {
			return;
		}

		camera = new MarkerEntity(EntityType.MARKER, client.world);
		camera.refreshPositionAndAngles(
				client.player.getX(),
				client.player.getEyeY(),
				client.player.getZ(),
				client.player.getYaw(),
				client.player.getPitch()
		);

		client.cameraEntity = camera;
		active = true;
	}

	private static void disable() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.cameraEntity = client.player;
		}
		camera = null;
		active = false;
	}

	/** Called from MouseMixin instead of the player's own look update while active. */
	public static void rotate(double dx, double dy) {
		if (camera == null) {
			return;
		}
		float yaw = camera.getYaw() + (float) (dx * MOUSE_SENSITIVITY);
		float pitch = camera.getPitch() + (float) (dy * MOUSE_SENSITIVITY);
		pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
		camera.setYaw(yaw);
		camera.setPitch(pitch);
	}

	public static void onClientTick(MinecraftClient client) {
		if (!active || camera == null) {
			return;
		}
		if (client.player == null || client.world == null) {
			forceDisable();
			return;
		}

		GameOptions options = client.options;
		double speed = options.sprintKey.isPressed() ? BASE_SPEED * SPRINT_MULTIPLIER : BASE_SPEED;

		double yawRad = Math.toRadians(camera.getYaw());
		double forwardX = -Math.sin(yawRad);
		double forwardZ = Math.cos(yawRad);
		double strafeX = Math.cos(yawRad);
		double strafeZ = Math.sin(yawRad);

		double moveX = 0.0D;
		double moveY = 0.0D;
		double moveZ = 0.0D;

		if (options.forwardKey.isPressed()) {
			moveX += forwardX;
			moveZ += forwardZ;
		}
		if (options.backKey.isPressed()) {
			moveX -= forwardX;
			moveZ -= forwardZ;
		}
		if (options.rightKey.isPressed()) {
			moveX += strafeX;
			moveZ += strafeZ;
		}
		if (options.leftKey.isPressed()) {
			moveX -= strafeX;
			moveZ -= strafeZ;
		}
		if (options.jumpKey.isPressed()) {
			moveY += 1.0D;
		}
		if (options.sneakKey.isPressed()) {
			moveY -= 1.0D;
		}

		// Normalize horizontal movement so diagonals aren't faster than straight lines.
		double horizontalLenSq = moveX * moveX + moveZ * moveZ;
		if (horizontalLenSq > 1.0E-6) {
			double invLen = 1.0 / Math.sqrt(horizontalLenSq);
			moveX *= invLen;
			moveZ *= invLen;
		}

		if (moveX == 0.0D && moveY == 0.0D && moveZ == 0.0D) {
			return;
		}

		double newX = camera.getX() + moveX * speed;
		double newY = camera.getY() + moveY * speed;
		double newZ = camera.getZ() + moveZ * speed;

		camera.refreshPositionAndAngles(newX, newY, newZ, camera.getYaw(), camera.getPitch());
	}

	/** Exposed for the player-freeze mixin, in case other code wants to gate on the live entity. */
	public static Entity getCameraEntity() {
		return camera;
	}
}
