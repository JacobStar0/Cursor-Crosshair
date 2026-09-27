package dev.JacobStar.cursorcrosshair.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
	private static final int CROSSHAIR_SIZE = 15;
	private static final Identifier CURSOR_CROSSHAIR_SPRITE =
			Identifier.withDefaultNamespace("hud/crosshair");

	@Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
	private void cursorCrosshair$hideCenteredCrosshair(GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker, CallbackInfo ci) {
		if (CursorCrosshairController.isActive()) {
			ci.cancel();
		}
	}

	@Redirect(
			method = "extractCrosshair",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 0
			)
	)
	private void cursorCrosshair$drawCenteredVanillaCrosshair(GuiGraphicsExtractor graphics,
			RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
		Window window = Minecraft.getInstance().getWindow();
		drawCrosshair(graphics, pipeline, sprite,
				CursorCrosshairController.centeredCrosshairX(window),
				CursorCrosshairController.centeredCrosshairY(window), width, height);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void cursorCrosshair$drawCrosshairOnTop(GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!CursorCrosshairController.isActive()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		graphics.nextStratum();
		drawCrosshair(graphics, RenderPipelines.CROSSHAIR, CURSOR_CROSSHAIR_SPRITE,
				CursorCrosshairController.crosshairXExact(minecraft),
				CursorCrosshairController.crosshairYExact(minecraft),
				CROSSHAIR_SIZE, CROSSHAIR_SIZE);
	}

	private static void drawCrosshair(GuiGraphicsExtractor graphics, RenderPipeline pipeline,
			Identifier sprite, double centerX, double centerY, int width, int height) {
		double left = roundToQuarterPixel(centerX - width / 2.0);
		double top = roundToQuarterPixel(centerY - height / 2.0);
		int x = (int)Math.floor(left);
		int y = (int)Math.floor(top);
		Matrix3x2fStack pose = graphics.pose();

		pose.pushMatrix();
		try {
			pose.translate((float)(left - x), (float)(top - y));
			graphics.blitSprite(pipeline, sprite, x, y, width, height);
		} finally {
			pose.popMatrix();
		}
	}

	private static double roundToQuarterPixel(double value) {
		return Math.round(value * 4.0) / 4.0;
	}
}
