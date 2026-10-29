package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BackgroundRenderer.class)
public class FogMixin {
    @ModifyVariable(method = "applyFog", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static float scx(float viewDistance) { return Scx.on("fog") ? (float) (viewDistance * Scx.v("fog", 0) / 100.0) : viewDistance; }
}
