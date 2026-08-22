package dev.JacobStar.cursorcrosshair.mixin;

import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
	private static final Identifier CURSOR_CROSSHAIR_SPRITE =
			Identifier.withDefaultNamespace("hud/crosshair");

	@Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
	private void cursorCrosshair$hideCenteredCrosshair(GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker, CallbackInfo ci) {
		if (CursorCrosshairController.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void cursorCrosshair$drawCrosshairOnTop(GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!CursorCrosshairController.isActive()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		int x = CursorCrosshairController.crosshairX(minecraft) - 7;
		int y = CursorCrosshairController.crosshairY(minecraft) - 7;
		graphics.nextStratum();
		graphics.blitSprite(RenderPipelines.CROSSHAIR, CURSOR_CROSSHAIR_SPRITE, x, y, 15, 15);
	}
}
