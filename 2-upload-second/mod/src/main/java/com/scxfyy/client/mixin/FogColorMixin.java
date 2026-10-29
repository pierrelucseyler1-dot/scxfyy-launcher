package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.render.BackgroundRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class FogColorMixin {
    @Inject(method = "getFogColor", at = @At("RETURN"))
    private static void scx(CallbackInfoReturnable<Vector4f> cir) {
        Vector4f v = cir.getReturnValue(); if (Scx.sky <= 0 || v == null) return;
        int rgb = Scx.skyRgb(); v.set(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, v.w);
    }
}
