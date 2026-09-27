package dev.JacobStar.cursorcrosshair.mixin;

import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 1100)
public abstract class MinecraftMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void cursorCrosshair$updateMode(CallbackInfo ci) {
		CursorCrosshairController.updateMode((Minecraft)(Object)this);
	}

	@Inject(
			method = "pick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
					shift = At.Shift.BEFORE
			)
	)
	private void cursorCrosshair$replacePick(float partialTick, CallbackInfo ci) {
		if (!CursorCrosshairController.isActive()) {
			return;
		}

		Minecraft minecraft = (Minecraft)(Object)this;
		HitResult hitResult = CursorCrosshairController.raycastFromCursor(partialTick);
		minecraft.hitResult = hitResult;
		minecraft.crosshairPickEntity = hitResult instanceof EntityHitResult entityHit
				? entityHit.getEntity() : null;
	}
}
