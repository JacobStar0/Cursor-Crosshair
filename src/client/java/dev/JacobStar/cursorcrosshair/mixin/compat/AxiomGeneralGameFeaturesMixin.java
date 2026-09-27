package dev.JacobStar.cursorcrosshair.mixin.compat;

import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.moulberry.axiom.GeneralGameFeatures", remap = false)
public abstract class AxiomGeneralGameFeaturesMixin {
	@ModifyArg(
			method = {"injectPickHead", "injectPickReturn"},
			at = @At(
					value = "INVOKE",
					target = "Lcom/moulberry/axiom/RayCaster;raycast(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;ZZ)Lcom/moulberry/axiom/RayCaster$RaycastResult;",
					remap = false
			),
			index = 2,
			require = 0,
			remap = false
	)
	private static Vec3 cursorCrosshair$useCursorDirection(Vec3 original) {
		return CursorCrosshairController.isActive()
				? CursorCrosshairController.cursorDirection(Minecraft.getInstance())
				: original;
	}

	@Redirect(
			method = "injectPickReturn",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;",
					remap = false
			),
			require = 0,
			remap = false
	)
	private static HitResult cursorCrosshair$pickWithLimitedReach(Entity entity,
			double range, float partialTick, boolean includeFluids) {
		if (CursorCrosshairController.isActive()) {
			HitResult hitResult = CursorCrosshairController.raycastBlockFromCursor(range, includeFluids);
			if (hitResult != null) {
				return hitResult;
			}
		}

		return entity.pick(range, partialTick, includeFluids);
	}
}
