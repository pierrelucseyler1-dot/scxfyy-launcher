package com.scxfyy.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntryListWidget.class)
public abstract class ListMixin {
    @Inject(method = "renderListBackground", at = @At("HEAD"), cancellable = true)
    private void scxBg(DrawContext g, CallbackInfo ci) {
        ClickableWidget w = (ClickableWidget) (Object) this;
        g.fill(w.getX(), w.getY(), w.getX() + w.getWidth(), w.getY() + w.getHeight(), 0x77100510); ci.cancel();
    }
    @Inject(method = "renderListSeparators", at = @At("HEAD"), cancellable = true)
    private void scxSep(DrawContext g, CallbackInfo ci) { ci.cancel(); }
}
