package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import com.scxfyy.client.Sunset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow public int width;
    @Shadow public int height;
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void scx(DrawContext g, int mx, int my, float d, CallbackInfo ci) {
        MinecraftClient c = MinecraftClient.getInstance(); Object self = this;
        boolean loading = self instanceof ConnectScreen || self instanceof DownloadingTerrainScreen || self instanceof ProgressScreen;
        if (c.world != null && !loading) { if (Scx.on("menublur")) return; Sunset.draw(g, width, height, 0.7f); }
        else Sunset.draw(g, width, height, 1f);
        ci.cancel();
    }
}
