package com.ruletaazul.mixin;

import com.ruletaazul.client.ClientState;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow private float yaw;
	@Shadow private float pitch;
	@Shadow protected abstract void setRotation(float yaw, float pitch);

	@Inject(method = "update", at = @At("RETURN"))
	private void ruletaazul$shake(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		float s = ClientState.shakeAmount();
		if (s <= 0f) return;
		double t = System.nanoTime() / 1.0e9;
		float dy = (float) (Math.sin(t * 43.0) * 0.6 + Math.sin(t * 77.0) * 0.4) * s;
		float dp = (float) (Math.sin(t * 51.0 + 1.3) * 0.6 + Math.sin(t * 89.0) * 0.4) * s;
		this.setRotation(this.yaw + dy, this.pitch + dp);
	}
}
