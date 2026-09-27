package dev.JacobStar.cursorcrosshair.mixin.compat;

import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.moulberry.axiom.capabilities.Bulldozer", remap = false)
public abstract class AxiomBulldozerMixin {
	@Shadow(remap = false) private static Vec3 lastView;

	@Inject(method = "handleInstabreak", at = @At("HEAD"), require = 0, remap = false)
	private static void cursorCrosshair$rememberCursorDirection(ClientLevel level, LocalPlayer player,
			MultiPlayerGameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
		if (CursorCrosshairController.isActive()) {
			lastView = CursorCrosshairController.cursorDirection(Minecraft.getInstance());
		}
	}

	@Redirect(
			method = "handleInstabreak",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
			),
			require = 0,
			remap = false
	)
	private static Vec3 cursorCrosshair$useCursorDirection(LocalPlayer player, float partialTick) {
		return CursorCrosshairController.isActive()
				? CursorCrosshairController.cursorDirection(Minecraft.getInstance())
				: player.getViewVector(partialTick);
	}
}
