package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class StarMixin {
    @Inject(method = "getStarBrightness", at = @At("HEAD"), cancellable = true)
    private void scx(CallbackInfoReturnable<Float> cir) { if (Scx.sky > 0) cir.setReturnValue(1.0f); }
}
