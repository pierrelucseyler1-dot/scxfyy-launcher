package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void scx(CallbackInfo ci) { if (Scx.on("bossbars")) ci.cancel(); }
}
