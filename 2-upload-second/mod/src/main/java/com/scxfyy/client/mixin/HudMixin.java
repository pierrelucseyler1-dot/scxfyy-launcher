package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void scxCross(CallbackInfo ci) { if (Scx.on("crosshair")) ci.cancel(); }
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void scxBoard(CallbackInfo ci) { if (Scx.on("scoreboard")) ci.cancel(); }
    @Inject(method = {"renderTitleAndSubtitle", "renderOverlayMessage"}, at = @At("HEAD"), cancellable = true)
    private void scxTitles(CallbackInfo ci) { if (Scx.on("titles")) ci.cancel(); }
    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void scxVignette(CallbackInfo ci) { if (Scx.on("cleanedges")) ci.cancel(); }
}
