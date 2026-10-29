package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
    private void scx(float td, CallbackInfoReturnable<Float> cir) { if (Scx.on("staticsky") && ((World) (Object) this).isClient) cir.setReturnValue(Scx.skyFrozen); }
}
