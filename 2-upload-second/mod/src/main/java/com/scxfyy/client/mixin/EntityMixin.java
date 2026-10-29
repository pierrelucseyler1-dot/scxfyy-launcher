package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void scx(double dx, double dy, CallbackInfo ci) {
        if (Scx.freelook && (Object) this instanceof ClientPlayerEntity) {
            float k = 0.15f * (float) (Scx.v("freelook", 0) / 100.0);
            Scx.fyaw += (float) dx * k;
            Scx.fpitch = MathHelper.clamp(Scx.fpitch + (float) dy * k, -90f, 90f);
            ci.cancel();
        }
    }
}
