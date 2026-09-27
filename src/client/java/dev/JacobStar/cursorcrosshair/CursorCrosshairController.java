package dev.JacobStar.cursorcrosshair;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import dev.JacobStar.cursorcrosshair.access.MouseHandlerAccess;
import org.joml.Vector3fc;
import org.lwjgl.sdl.SDLMouse;

public final class CursorCrosshairController {
	private static final long CAMERA_LOOK_HOLD_NANOS = 180_000_000L;

	public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("cursor_crosshair", "general")
	);
	public static final KeyMapping ACTIVATE_KEY = new KeyMapping(
			"key.cursor_crosshair.activate",
			InputConstants.Type.KEYBOARD,
			InputConstants.KEY_LALT,
			KEY_CATEGORY
	);

	private static boolean active;
	private static boolean cameraLookPending;
	private static boolean cameraLookActive;
	private static long cameraLookPressedAt;
	private static double heldCrosshairX;
	private static double heldCrosshairY;

	private CursorCrosshairController() {
	}

	public static boolean isActive() {
		return active;
	}

	public static boolean isCameraLookActive() {
		return cameraLookActive;
	}

	public static void updateMode(Minecraft minecraft) {
		Window window = minecraft.getWindow();
		boolean shouldBeActive = ACTIVATE_KEY.isDown()
				&& minecraft.player != null
				&& minecraft.level != null
				&& minecraft.gui.screen() == null
				&& minecraft.gui.overlay() == null
				&& minecraft.mouseHandler.isMouseGrabbed()
				&& window.isFocused();

		if (shouldBeActive == active) {
			if (active && cameraLookPending
					&& System.nanoTime() - cameraLookPressedAt >= CAMERA_LOOK_HOLD_NANOS) {
				beginCameraLook(minecraft);
			}
			return;
		}

		MouseHandlerAccess mouseAccess = (MouseHandlerAccess)minecraft.mouseHandler;
		mouseAccess.cursorCrosshair$resetMovement();
		minecraft.mouseHandler.setIgnoreFirstMove();

		if (shouldBeActive) {
			double centerX = centeredCrosshairX(window)
					* window.getScreenWidth() / window.getGuiScaledWidth();
			double centerY = centeredCrosshairY(window)
					* window.getScreenHeight() / window.getGuiScaledHeight();
			active = true;
			releaseMouseHidden(window, centerX, centerY);
			mouseAccess.cursorCrosshair$setPosition(centerX, centerY);
		} else if (minecraft.mouseHandler.isMouseGrabbed()) {
			// Recentring here would be reported as camera movement on the next frame.
			grabMouse(window, minecraft.mouseHandler.xpos(), minecraft.mouseHandler.ypos());
			cameraLookPending = false;
			cameraLookActive = false;
			active = false;
		} else {
			cameraLookPending = false;
			cameraLookActive = false;
			active = false;
		}
	}

	public static int crosshairX(Minecraft minecraft) {
		return (int)Math.round(crosshairXExact(minecraft));
	}

	public static int crosshairY(Minecraft minecraft) {
		return (int)Math.round(crosshairYExact(minecraft));
	}

	public static double crosshairXExact(Minecraft minecraft) {
		if (cameraLookPending || cameraLookActive) {
			return heldCrosshairX;
		}
		return minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
	}

	public static double crosshairYExact(Minecraft minecraft) {
		if (cameraLookPending || cameraLookActive) {
			return heldCrosshairY;
		}
		return minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
	}

	public static boolean handleCameraLook(MouseButtonInfo button, int action) {
		if (!active || button.button() != InputConstants.MOUSE_BUTTON_MIDDLE) {
			return false;
		}

		if (action == InputConstants.PRESS && !cameraLookPending && !cameraLookActive) {
			Minecraft minecraft = Minecraft.getInstance();
			heldCrosshairX = crosshairXExact(minecraft);
			heldCrosshairY = crosshairYExact(minecraft);
			cameraLookPressedAt = System.nanoTime();
			cameraLookPending = true;
		} else if (action == InputConstants.RELEASE && cameraLookActive) {
			endCameraLook(Minecraft.getInstance());
		} else if (action == InputConstants.RELEASE && cameraLookPending) {
			Minecraft minecraft = Minecraft.getInstance();
			cameraLookPending = false;
			restoreHeldCrosshair(minecraft, false);
			KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE));
		}

		// Middle click belongs to camera control while free-crosshair mode is active.
		return true;
	}

	private static void beginCameraLook(Minecraft minecraft) {
		cameraLookPending = false;
		cameraLookActive = true;
		MouseHandlerAccess mouseAccess = (MouseHandlerAccess)minecraft.mouseHandler;
		mouseAccess.cursorCrosshair$resetMovement();
		minecraft.mouseHandler.setIgnoreFirstMove();
		Window window = minecraft.getWindow();
		grabMouse(window, minecraft.mouseHandler.xpos(), minecraft.mouseHandler.ypos());
	}

	private static void endCameraLook(Minecraft minecraft) {
		cameraLookActive = false;
		restoreHeldCrosshair(minecraft, true);
	}

	private static void restoreHeldCrosshair(Minecraft minecraft, boolean restoreHiddenCursorMode) {
		Window window = minecraft.getWindow();
		double cursorX = heldCrosshairX * window.getScreenWidth() / window.getGuiScaledWidth();
		double cursorY = heldCrosshairY * window.getScreenHeight() / window.getGuiScaledHeight();
		MouseHandlerAccess mouseAccess = (MouseHandlerAccess)minecraft.mouseHandler;

		mouseAccess.cursorCrosshair$resetMovement();
		minecraft.mouseHandler.setIgnoreFirstMove();
		if (restoreHiddenCursorMode) {
			releaseMouseHidden(window, cursorX, cursorY);
		} else {
			SDLMouse.SDL_WarpMouseInWindow(window.handle(), (float)cursorX, (float)cursorY);
		}
		mouseAccess.cursorCrosshair$setPosition(cursorX, cursorY);
	}

	private static void releaseMouseHidden(Window window, double x, double y) {
		InputConstants.releaseMouse(window, x, y);
		SDLMouse.SDL_HideCursor();
	}

	private static void grabMouse(Window window, double x, double y) {
		// Relative mouse mode hides the pointer and restores this visible state when released.
		SDLMouse.SDL_ShowCursor();
		InputConstants.grabMouse(window, x, y);
	}

	public static double centeredCrosshairX(Window window) {
		return window.getWidth() / (double)window.getGuiScale() / 2.0;
	}

	public static double centeredCrosshairY(Window window) {
		return window.getHeight() / (double)window.getGuiScale() / 2.0;
	}

	public static boolean handleHotbarClick(MouseButtonInfo button, int action) {
		if (!active || button.button() != InputConstants.MOUSE_BUTTON_LEFT || action != InputConstants.PRESS) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return false;
		}

		Window window = minecraft.getWindow();
		int mouseX = crosshairX(minecraft);
		int mouseY = crosshairY(minecraft);
		int hotbarLeft = window.getGuiScaledWidth() / 2 - 90;
		int hotbarTop = window.getGuiScaledHeight() - 23;

		if (mouseY < hotbarTop || mouseY >= window.getGuiScaledHeight()
				|| mouseX < hotbarLeft || mouseX >= hotbarLeft + 180) {
			return false;
		}

		player.getInventory().setSelectedSlot((mouseX - hotbarLeft) / 20);
		return true;
	}

	public static HitResult raycastFromCursor(float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		Entity cameraEntity = minecraft.getCameraEntity();
		if (!active || player == null || cameraEntity == null || minecraft.level == null) {
			return minecraft.hitResult;
		}

		Camera camera = minecraft.gameRenderer.mainCamera();
		Vec3 start = camera.position();
		Vec3 direction = cursorDirection(minecraft, camera);
		double blockRange = player.blockInteractionRange();
		double entityRange = player.entityInteractionRange();
		double maxRange = Math.max(blockRange, entityRange);
		Vec3 end = start.add(direction.scale(maxRange));

		BlockHitResult blockHit = minecraft.level.clip(new ClipContext(start, end,
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, cameraEntity));
		double blockDistanceSquared = blockHit.getType() == HitResult.Type.MISS
				? maxRange * maxRange : start.distanceToSqr(blockHit.getLocation());

		AABB searchBox = cameraEntity.getBoundingBox()
				.expandTowards(direction.scale(maxRange)).inflate(1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(cameraEntity, start, end,
				searchBox, EntitySelector.CAN_BE_PICKED, blockDistanceSquared);

		if (entityHit != null && start.distanceToSqr(entityHit.getLocation()) <= entityRange * entityRange) {
			return entityHit;
		}
		if (blockHit.getType() != HitResult.Type.MISS
				&& start.distanceToSqr(blockHit.getLocation()) <= blockRange * blockRange) {
			return blockHit;
		}

		Vec3 missLocation = start.add(direction.scale(blockRange));
		Direction missDirection = Direction.getApproximateNearest(direction.x, direction.y, direction.z);
		return BlockHitResult.miss(missLocation, missDirection, BlockPos.containing(missLocation));
	}

	public static HitResult raycastBlockFromCursor(double range, boolean includeFluids) {
		Minecraft minecraft = Minecraft.getInstance();
		Entity cameraEntity = minecraft.getCameraEntity();
		if (!active || cameraEntity == null || minecraft.level == null) {
			return null;
		}

		Camera camera = minecraft.gameRenderer.mainCamera();
		Vec3 start = camera.position();
		Vec3 end = start.add(cursorDirection(minecraft, camera).scale(range));
		return minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
				includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, cameraEntity));
	}

	public static Vec3 cursorDirection(Minecraft minecraft) {
		return cursorDirection(minecraft, minecraft.gameRenderer.mainCamera());
	}

	private static Vec3 cursorDirection(Minecraft minecraft, Camera camera) {
		Window window = minecraft.getWindow();
		double mouseX = crosshairXExact(minecraft);
		double mouseY = crosshairYExact(minecraft);
		double normalizedX = mouseX / window.getGuiScaledWidth() * 2.0 - 1.0;
		double normalizedY = 1.0 - mouseY / window.getGuiScaledHeight() * 2.0;
		double tangent = Math.tan(Math.toRadians(camera.getFov()) * 0.5);
		double aspect = (double)window.getWidth() / Math.max(1, window.getHeight());

		Vector3fc forward = camera.forwardVector();
		Vector3fc left = camera.leftVector();
		Vector3fc up = camera.upVector();
		return new Vec3(forward.x(), forward.y(), forward.z())
				.add(new Vec3(left.x(), left.y(), left.z()).scale(-normalizedX * tangent * aspect))
				.add(new Vec3(up.x(), up.y(), up.z()).scale(normalizedY * tangent))
				.normalize();
	}
}
