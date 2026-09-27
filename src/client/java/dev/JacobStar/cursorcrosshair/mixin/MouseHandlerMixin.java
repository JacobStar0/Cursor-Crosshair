package dev.JacobStar.cursorcrosshair.mixin;

import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import dev.JacobStar.cursorcrosshair.access.MouseHandlerAccess;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin implements MouseHandlerAccess {
	@Shadow private double accumulatedDX;
	@Shadow private double accumulatedDY;
	@Shadow private double xpos;
	@Shadow private double ypos;

	@Override
	public void cursorCrosshair$resetMovement() {
		this.accumulatedDX = 0.0;
		this.accumulatedDY = 0.0;
	}

	@Override
	public void cursorCrosshair$setPosition(double x, double y) {
		this.xpos = x;
		this.ypos = y;
		this.accumulatedDX = 0.0;
		this.accumulatedDY = 0.0;
	}

	@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
	private void cursorCrosshair$freezeCamera(double frameTime, CallbackInfo ci) {
		if (CursorCrosshairController.isActive() && !CursorCrosshairController.isCameraLookActive()) {
			this.accumulatedDX = 0.0;
			this.accumulatedDY = 0.0;
			ci.cancel();
		}
	}

	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void cursorCrosshair$clickHotbar(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
		if (CursorCrosshairController.handleCameraLook(button, action)) {
			ci.cancel();
			return;
		}

		if (CursorCrosshairController.handleHotbarClick(button, action)) {
			ci.cancel();
		}
	}
}
