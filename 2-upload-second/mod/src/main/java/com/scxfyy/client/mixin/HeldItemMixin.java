package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void scxPush(AbstractClientPlayerEntity pl, float td, float pitch, Hand hand, float swing, ItemStack s, float equip, MatrixStack m, VertexConsumerProvider vp, int light, CallbackInfo ci) {
        m.push();
        if (Scx.on("viewmodel")) { float sc = (float) Scx.m("viewmodel").nums.get(0).v / 100f; m.translate(Scx.m("viewmodel").nums.get(1).v / 100.0, Scx.m("viewmodel").nums.get(2).v / 100.0, 0); m.scale(sc, sc, sc); }
    }
    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void scxPop(AbstractClientPlayerEntity pl, float td, float pitch, Hand hand, float swing, ItemStack s, float equip, MatrixStack m, VertexConsumerProvider vp, int light, CallbackInfo ci) { m.pop(); }
}
