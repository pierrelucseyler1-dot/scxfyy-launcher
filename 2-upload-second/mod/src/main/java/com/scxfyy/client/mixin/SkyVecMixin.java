package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class SkyVecMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void scx(CallbackInfoReturnable<Vec3d> cir) { if (Scx.sky > 0) cir.setReturnValue(Vec3d.unpackRgb(Scx.skyRgb())); }
}
