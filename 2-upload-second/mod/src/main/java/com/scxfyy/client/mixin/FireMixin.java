package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class FireMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void scxPush(MatrixStack m, VertexConsumerProvider vp, CallbackInfo ci) { m.push(); if (Scx.on("lowfire")) m.translate(0, -0.5 * Scx.v("lowfire", 0) / 100.0, 0); }
    @Inject(method = "renderFireOverlay", at = @At("RETURN"))
    private static void scxPop(MatrixStack m, VertexConsumerProvider vp, CallbackInfo ci) { m.pop(); }
}
