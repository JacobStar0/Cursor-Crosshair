package dev.JacobStar.cursorcrosshair.mixin;

import dev.JacobStar.cursorcrosshair.CursorCrosshairController;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;

@Mixin(Options.class)
public abstract class OptionsMixin {
	@Shadow @Final @Mutable public KeyMapping[] keyMappings;

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Options;load()V",
					shift = At.Shift.BEFORE
			)
	)
	private void cursorCrosshair$registerKeyMapping(Minecraft minecraft, File optionsFile, CallbackInfo ci) {
		this.keyMappings = Arrays.copyOf(this.keyMappings, this.keyMappings.length + 1);
		this.keyMappings[this.keyMappings.length - 1] = CursorCrosshairController.ACTIVATE_KEY;
	}
}
